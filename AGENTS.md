# Kortex — Project Context for Coding Agents

> This document describes what Kortex is meant to become, which decisions
> are agreed, and what is still unproven. The repository is the source of
> truth for what exists today; this document is the source of truth for
> direction. Every statement here is one of four kinds, and reports should
> keep them apart: **fact** (verified in the repository or by a check),
> **decision** (agreed with the project owner), **proposal** (a candidate
> design that must be validated), **open question**.

Last revised: 2026-11-20.

## 1. Product vision

Kortex makes Kotlin an automation language for Home Assistant (HA), in the
way Node-RED offers its own automation model next to HA's built-in one.

A user opens Kortex inside the HA UI, writes an automation in Kotlin in a
built-in editor, saves it, and it runs. The automation is ordinary Kotlin:
it can use libraries, coroutines and Flows. In HA it appears as a native
automation that can be disabled, is logged and traced, and can be used
from other automations.

## 2. Goals

1.  **Kotlin as the automation language.** Triggers, conditions and
    actions are all written in Kotlin. Users do not write YAML or Jinja.
2.  **Editor inside the HA UI.** Write, build, run and inspect automations
    without leaving HA.
3.  **Full Kotlin.** Automations can depend on external libraries
    (see §5.3 for what "any library" means on each runtime).
4.  **Native presence in HA.** Each Kotlin automation is visible under
    HA's automations, can be enabled and disabled there, leaves traces
    and logbook entries, and can be triggered by other automations.
5.  **Works out of the box** on a stock Home Assistant OS installation.
    No forks or patched builds of HA.
6.  **Later: YAML round-trip.** Open an existing YAML automation as
    Kotlin, edit it, and save it back as plain YAML (§5.8).

## 3. Decisions

| Topic | Decision |
|---|---|
| Delivery | HA add-on (runtime + UI through Ingress), plus a companion custom integration only where a concrete need is shown (§5.5) |
| Supported installs | Home Assistant OS first. HA Container later (§5.9) |
| Runtime | JVM first. Kotlin/Native is a possible later option, not a current requirement (§5.7) |
| Authoring | The whole automation is written in Kotlin; Kortex generates everything HA needs from it |
| HA presence | A generated native HA automation ("stub") per Kotlin automation: HA owns the triggers and the enabled state, Kortex runs the Kotlin body (§5.1) |
| Unit | Each automation is independent: its own source, dependencies, build, deploy and enabled state |
| Dependencies | Declared in the source with script-style annotations, e.g. `@file:DependsOn("group:artifact:version")`. A Gradle-based build is acceptable as a starting point if the annotation route is not ready (§5.3) |
| Editor | Lightweight built-in web editor. Not an embedded VS Code / code-server (§5.4) |
| YAML round-trip | Later, after the core works (§5.8) |
| Build tooling for this repo | Gradle Kotlin DSL, version catalog in `gradle/libs.versions.toml` |

### Superseded — do not reintroduce without agreement

An earlier revision of this document chose the items below. They are no
longer the plan:

-   Kotlin/Native as the primary compilation target for user code.
-   A hybrid local/remote builder and a remote Compiler Service.
-   One native OS process per program, supervised by the Runtime.
-   Runtime-to-program IPC over Unix domain sockets.
-   "Standalone programs" and "native automations" as two separate
    user-facing execution models. There is now one user-facing concept:
    a Kotlin automation.

## 4. Repository snapshot (facts, verified 2026-11-20)

Gradle modules: `kortex-core`, `kortex-compose`, `kortex-sample`,
`kortex-stub-proof`. There is no runtime, add-on, custom integration,
editor or automation-definition module.

Versions from the catalog: Kotlin 2.4.21, Ktor 3.6.1, Koin 4.2.2,
Compose 1.12.2, Kotest 6.2.7. Group `com.ucasoft.kortex`, version `0.0.1`,
JDK toolchain 17.

### 4.1 `kortex-core`

Kotlin Multiplatform library: HA WebSocket client, HA REST client, auth,
request/response models, typed entity wrappers, state and event models,
service calls, config loading, Flow-based observation, Koin setup.

-   Declared targets: JVM, `linuxX64`, `linuxArm64`, `mingwX64`,
    `macosArm64`, JS (browser, Node.js), `iosArm64`,
    `iosSimulatorArm64`. Only `jvmTest` tests exist. A declared target is
    not evidence that it compiles, links or runs: the JS target does not
    compile today (`ConfigLoader.web.kt` references
    `_root_ide_package_`), and the native targets have not been tried.
-   Entry points: `startKortex` (callback API) and `startKortexObservable`
    (gives an `Entities` object backed by a `StateFlow` of all states).
-   `Entities` exposes per-domain lists (`lights`, `switches`, …) of typed
    wrappers such as `ToggleableEntity` with `turnOn()`, `turnOff()`,
    `toggle()`, `onToggled { }`.
-   Two clients, in separate packages:
    -   `client.websocket.WSKortexClient` opens the WebSocket and hands
        out a `KortexContext` for WebSocket commands.
    -   `client.rest.RestKortexClient` offers `getAutomationConfig`,
        `saveAutomationConfig`, `deleteAutomationConfig` (the automation
        config endpoint of §5.1) and `buildImageUrl`. It needs no open
        WebSocket.
-   The Koin module `clientModule` provides two named Ktor clients,
    `websocketClient` and `restClient`. Each carries its connection
    settings as a default request; the REST one also carries the bearer
    token.

Properties of the current Core that matter for the plan below:

-   The WebSocket path `api/websocket` is hardcoded in the default
    request of `websocketClient`, and the REST paths start with `api/`.
    The Supervisor proxy an add-on uses has different paths (§5.2), so
    both must become configurable.
-   Only `state_changed` events can be subscribed to (`EventType` has one
    value), and every incoming event is decoded as a state change. A
    custom event, such as a stub hand-off (§5.1), cannot be received yet.
-   Core's public API uses kotlinx.coroutines and kotlinx.serialization
    types, but both arrive through `implementation` dependencies. A
    consumer that depends only on Core cannot compile against them.
-   `startKortex` calls the global `startKoin`, so only one client can be
    started per process. `RestKortexClient` also resolves its Ktor client
    and config from the global Koin.
-   `KortexContext` is a concrete class holding a Ktor
    `DefaultClientWebSocketSession`; every entity wrapper takes it
    directly.
-   Failed requests and unknown frames are only `println`ed. A failed
    `callService` is not reported to the caller.
-   `KortexContext.requestIds` and `pendingRequests` are plain mutable
    maps touched from both callers and the reader loop.
-   `Entities` captures the set of entity ids once, at creation. Entities
    added to HA later are not picked up.
-   There is no trigger/condition/action automation API. Automations today
    are free-form code inside `startKortexObservable { }`.

### 4.2 `kortex-compose` and `kortex-sample`

`kortex-compose` holds Compose integration utilities over Core.
`kortex-sample` holds runnable samples (terminal status view, desktop
Compose app). Neither is the add-on UI or the editor.

### 4.3 `kortex-stub-proof`

A throwaway JVM program for Phase 0 (§7), not the Runtime. Each run looks
up the HA automation with id `kortex_proof_stub` through
`RestKortexClient`: if it is missing the run creates it, otherwise the
run deletes it. The stub has an event trigger (`kortex_proof_trigger`)
and one action that fires the event `kortex_proof_hand_off` with the
automation id. It writes to the HA instance configured through
`application.conf`, so run it only against an instance where that is
acceptable.

## 5. Architecture

```text
Home Assistant Core
 ├─ automation.<kortex stub>     generated; triggers + enabled state + trace
 ├─ WebSocket / REST API
 └─ (optional) Kortex custom integration
            ▲
            │ Supervisor proxy
            ▼
Kortex add-on (one container)
 ├─ Runtime (JVM)
 │   ├─ single HA connection (kortex-core)
 │   ├─ automation registry and lifecycle
 │   ├─ build + dependency resolution
 │   ├─ automation host (runs the Kotlin bodies)
 │   └─ stub synchronisation with HA
 ├─ Web UI / editor (Ingress)
 └─ Workspace: automation sources, build cache, logs
```

This is a logical picture. Do not create Gradle modules to match it until
the phase that needs them (§7).

### 5.1 Native stub (decision on the approach; mechanism is a proposal)

For each Kotlin automation, Kortex creates and maintains a real HA
automation:

-   it carries the triggers extracted from the Kotlin definition;
-   its action hands control to Kortex, passing the automation id and the
    trigger data;
-   Kortex then runs the Kotlin body.

What this gives: the automation is listed in HA, can be disabled there
(HA then stops firing it), has `last_triggered`, traces and logbook
entries, and can be started from other automations with
`automation.trigger`.

What it does not give: the trace shows the trigger and the hand-off, not
what happened inside Kotlin. Kotlin logs and errors are Kortex's
responsibility. Do not describe Kotlin internals as natively traced.

Everything below is **proposal / to prototype**:

-   **Creating stubs.** Candidate: HA's automation config endpoint
    (`/api/config/automation/config/<id>`), which HA's own automation
    editor uses and which writes to `automations.yaml`. It is not a
    documented public API; verify behaviour and stability against the HA
    versions to be supported, and check what happens when the user's
    configuration does not include `automations.yaml`.
-   **Hand-off to Kortex.** Two candidates:
    (a) the stub fires an HA event that the Runtime receives over its
    WebSocket subscription — needs no custom integration, but HA does
    not learn whether the Kotlin body succeeded;
    (b) the stub calls an action registered by a companion integration,
    which forwards to the Runtime and can report failure back into the
    trace. A WebSocket API client cannot register actions, so (b)
    requires the integration.
-   **Kotlin-side triggers.** Some triggers cannot be expressed as HA
    triggers (for example a combination of Flows). Candidate: the Runtime
    evaluates them, respects the stub's enabled state, and starts the run
    through `automation.trigger` so HA still records it.
-   **Trigger extraction.** Triggers must be obtainable from the Kotlin
    definition without running the automation body. This shapes the
    automation API (§5.6).
-   **Ownership.** Stubs are generated. Mark them as managed by Kortex,
    define what happens to manual edits made in HA, remove stubs when the
    Kotlin automation is deleted, and avoid id collisions with the user's
    own automations.
-   Generated stubs may contain templates to pass trigger data. That is
    fine; the goal is that users never write them.

Checked so far (facts; each states how it was checked):

-   On HA 2026.11.2, with the Phase 0 proof: a `POST` to the config
    endpoint with a free-form string id created a stub, and a `GET`
    returned it with the id added. HA loaded it at once, without a
    restart, and enabled it. The entity id came from the alias, not from
    the id. That instance includes `automations.yaml`; the case without
    it is still unchecked.
-   Delete through the endpoint is covered by unit tests only; its effect
    on a real instance is not recorded yet.
-   From the HA source at tag 2026.11.2, not yet tried on an instance:
    -   The endpoint is admin-only, rewrites the whole `automations.yaml`
        (so hand-written comments in it are lost) and then reloads that
        one automation. `DELETE` also removes the entity.
    -   `automation.trigger` runs the actions even when the automation is
        turned off; only real triggers check the enabled state. A
        Kotlin-side trigger must therefore check the stub's state itself.
    -   Each run fires `automation_triggered` with the stub id and a run
        id.
    -   Traces can be read only through the WebSocket commands
        `trace/list` and `trace/get`.
-   From HA's official developer documentation, read 2026-11-20: neither
    the config endpoint nor the trace commands are documented. The
    WebSocket commands `subscribe_trigger` and `fire_event` are.
-   From the HA source at older tags: the same config view is registered
    in 0.50.2, 0.100.0 and 2021.1.0. The payload those versions accept
    was not checked.

Documented alternatives to the config endpoint, not evaluated yet:
writing the stub YAML into HA's config directory and calling
`automation.reload`; or receiving HA triggers through `subscribe_trigger`
with no stub, which gives up the native automation of goal 4.

### 5.2 Runtime

One JVM process inside the add-on. It owns the HA connection, the
automation registry, builds, loading and unloading, logs and stub sync.

-   **HA connection.** In an add-on, reach HA through the Supervisor proxy
    (`ws://supervisor/core/websocket` and `http://supervisor/core/api`,
    authenticated with `SUPERVISOR_TOKEN`; the add-on must request HA API
    access with `homeassistant_api: true`). HA's add-on documentation
    confirms these (read 2026-11-20); they have not been tried from an
    add-on. Keep Supervisor specifics
    behind a small adapter so the Container case (§5.9) can supply a URL
    and long-lived token instead.
-   **Hosting model (proposal).** All automations run in the one Runtime
    JVM. Each is loaded from its own build output in its own class loader
    and runs in its own supervised coroutine scope. This keeps
    per-automation dependencies, builds and deploys independent at the
    cost of one JVM rather than one per automation.
-   **Isolation limits.** An exception in one automation must not affect
    others. In-process hosting cannot protect against memory exhaustion,
    blocked threads or a call that exits the process. Say so; do not
    present it as a sandbox. Automations are the user's own code on the
    user's own machine.
-   **No IPC and no per-automation credentials.** Automations use the
    Runtime's connection through the Core API in-process.

Deployment rules (decision):

1.  A build failure leaves the running version untouched.
2.  Two versions of the same automation never run at the same time.
    Replace means stop old, start new.
3.  If the new version fails to start, restore the previous one.

### 5.3 Build and dependencies

Users declare libraries in the automation source with annotations
(`@file:DependsOn(...)`, and a repository annotation if needed). The
Runtime resolves them from Maven repositories and compiles on the HA host.

Mechanism candidates, to be chosen by measurement in Phase 1:

-   **A.** Kotlin scripting host with Maven dependency resolution.
    Lightest to implement. JVM-only, and JetBrains has narrowed its
    scripting support in the past; check its status for the Kotlin
    version in use.
-   **B.** Kortex reads the annotations itself, resolves artifacts, and
    compiles regular Kotlin sources with the embeddable compiler.
-   **C.** Kortex generates a Gradle project per automation. Heaviest,
    but accepted as a starting point.

Selection criteria: cold and warm build time and peak memory on
Raspberry Pi 4-class ARM64 hardware and on amd64, quality of compile
errors for the editor, and maintenance outlook.

Treat the annotations as a Kortex-owned dependency declaration rather
than as a feature of one tool. That keeps a later Native builder possible
without changing user code.

What "any Kotlin Multiplatform library" means: on the JVM runtime, any
library that publishes a JVM artifact (which includes nearly all KMP
libraries and all Java libraries). Libraries that publish only Native, JS
or iOS targets cannot be used. State this limit rather than promising
"any".

### 5.4 Editor

A lightweight web editor served by the add-on through Ingress: automation
list, source editor, save/build/run, build errors, logs, enabled state.

-   First version: syntax highlighting and compiler diagnostics.
-   Code completion and other language intelligence come later. A Kotlin
    language server is a heavy JVM process; whether and how to offer it
    on small hardware is open.
-   The editor technology is not chosen.
-   `kortex-compose` is not the editor.

### 5.5 Companion custom integration

Build it only for a demonstrated need. Known candidates: reporting Kotlin
failures back into the HA trace (§5.1 option b), and exposing status as
entities. An add-on cannot install a custom integration by itself in the
normal way, so every function that depends on the integration weakens
"works out of the box". Prefer designs where the add-on alone delivers
the core function.

### 5.6 Automation API

Core has no trigger/condition/action API today. One must be designed so
that:

-   the user writes the whole automation in Kotlin;
-   triggers can be extracted without running the body (§5.1);
-   the body can use the existing typed entities, Flows and service calls;
-   it lives in common Kotlin code where practical (§5.7).

This is a new public API. Propose it explicitly and get agreement before
implementing. Do not invent DSL methods in examples or documentation as
if they existed.

### 5.7 Kotlin/Native (later)

Not a current requirement. Known constraints if it is revisited:

-   Kotlin/Native 2.4.21 publishes compiler distributions only for Linux
    x86_64, macOS and Windows (fact, checked on Maven Central). It cannot
    compile on an ARM64 HA host, so Native builds there need a separate
    build machine or service.
-   Only libraries that publish `linuxX64`/`linuxArm64` targets can be
    used.
-   Per-automation processes become cheap, which fits the independent
    automation unit.

The only thing to do now is avoid needless obstacles: keep the user-facing
API in Core's common code where practical. Do not add Native build
infrastructure.

### 5.8 YAML round-trip (later)

A separate, declarative Kotlin representation that maps directly onto
HA's automation schema: open a YAML automation as Kotlin, edit, save back
as plain YAML that runs with no Kortex involvement.

Limits to keep honest: only what the HA schema can express; templates
stay as strings; constructs Kortex does not understand must be preserved
or the file refused, never silently dropped. Do not start this before the
core flow works.

### 5.9 HA Container (later)

The Runtime and UI should be runnable as an ordinary container configured
with an HA URL and a long-lived token, with the UI on its own port. Core's
existing config loading already supports hostname, port and token. Do not
scatter Supervisor-only assumptions through the code.

## 6. Open questions — do not silently decide

1.  Stub creation mechanism and its stability across HA versions.
2.  Hand-off from stub to Kortex: event or integration action; how
    failures surface in HA.
3.  Kotlin-side triggers and how they respect the stub's enabled state.
4.  Stub ownership: manual edits, renames, deletion, id scheme.
5.  Shape of the automation API (§5.6).
6.  Build mechanism A, B or C (§5.3) and the resulting build times.
7.  Class-loader hosting: unloading, leaked threads, shared versus
    per-automation copies of Core and kotlinx libraries.
8.  State across reloads: in-memory timers and state are lost on every
    redeploy and restart. Is persistence needed?
9.  Startup health check and rollback criteria for an automation.
10. Role and distribution of the custom integration.
11. Editor technology and the path to code completion.
12. Workspace location and layout on disk, backup behaviour.
13. Minimum supported hardware and memory budget.

## 7. Implementation sequence

Work in small, verifiable steps. Do not scaffold the whole product.

### Phase 0 — Stub proof (highest risk, do first)

With a small JVM program on Core against a real HA instance: create a
stub automation, have its trigger reach Kotlin code, confirm that
disabling it in HA stops the Kotlin code, confirm the trace and
`automation.trigger`, update it, delete it. Make the WebSocket path
configurable in Core as needed.

Deliverable: a written result for open questions 1–4 with the HA version
tested.

Status on 2026-11-20: the proof program exists (§4.3). Creating a stub is
done and was run against HA 2026.11.2; deleting is implemented but not
yet recorded against an instance (§5.1). Still open: the trigger reaching
Kotlin code (blocked on custom events in Core, §4.1), disabling, the
trace, `automation.trigger`, and updating. The written result does not
exist yet.

### Phase 1 — Build and load proof

Compile one automation source that declares an external dependency, load
it into a running JVM, run it, replace it, unload it. Measure build time
and memory on amd64 and ARM64.

Deliverable: choice among §5.3 A/B/C with numbers.

### Phase 2 — Runtime

Automation registry, lifecycle, the deployment rules of §5.2, per-automation
logs, stub synchronisation, and the automation API of §5.6 once agreed.
Fix the Core properties in §4.1 that block this (error reporting, single
global Koin start, late-added entities).

### Phase 3 — Add-on and editor

Package the Runtime as an HA OS add-on for amd64 and aarch64 with Ingress
and the first editor (§5.4).

### Phase 4 — As justified

Companion integration, editor language intelligence, YAML round-trip,
HA Container packaging, Kotlin/Native.

## 8. Boundaries

Unless explicitly agreed otherwise:

-   Do not move runtime, build or UI responsibilities into `kortex-core`.
-   Do not make `kortex-compose` the add-on editor.
-   Do not claim native HA traces for what happens inside Kotlin code.
-   Do not require a modified Home Assistant.
-   Do not reintroduce the superseded items in §3.
-   Do not invent public APIs, DSL methods or entity accessors that are
    not in the repository; label new ones as proposals.
-   Do not assume a declared Kotlin Multiplatform target works.
-   Do not create modules ahead of the phase that needs them.
-   Never commit HA tokens, credentials or private endpoints.

## 9. Instructions for coding agents

1.  **Inspect before changing.** Read the relevant sources, Gradle
    scripts, version catalog and tests first. Do not invent dependency
    versions or plugin configuration.
2.  **Make changes in small steps.** Start with a minimal skeleton for
    the current task only, then fill it in one piece of functionality at
    a time. If a piece needs another function that does not exist yet,
    add that as a stub first (`TODO("...")`, never a silent default).
    The code must compile after every step. Stop for review after each
    step, and list any remaining stubs in the report.
3.  **Preserve existing public APIs** unless the task calls for a
    breaking change.
4.  **Verify claims about HA and platforms.** HA behaviour stated in this
    document as a proposal must be checked against a real instance or
    HA's source and documentation, with the version noted.
5.  **Raise conflicts.** If evidence contradicts a decision in §3,
    explain the conflict and present options before changing course.
6.  **Report verification precisely.** State what was built and run, on
    which host and target, against which HA version, and what remains
    unverified. Do not call a feature complete when only its interfaces
    or scaffolding exist.

When reporting implementation work, use: **What changed**, **Why**,
**Files/modules affected**, **Verification performed**,
**Known limitations / follow-up**.
