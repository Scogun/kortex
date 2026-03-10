# Kortex

**Kortex** is a Kotlin Multiplatform client toolkit for Home Assistant.
It provides typed entities, reactive state streams, and Compose integration for building automation apps and tools.

![GitHub](https://img.shields.io/github/license/Scogun/kortex?color=blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue)
![Ktor](https://img.shields.io/badge/Ktor-3.4.1-blue)
![Compose](https://img.shields.io/badge/Compose-1.10.2-blue)

---
* [Features](#features)
* [Supported Targets](#supported-targets)
* [Modules](#modules)
* [Quick Start](#quick-start)
  * [Requirements](#requirements)
  * [Configuration](#configuration)
  * [Run](#run)
* [Usage](#usage)
  * [Observable Startup](#observable-startup)
    * [Live Terminal Status Table](#live-terminal-status-table)
  * [Compose Startup](#compose-startup)
* [License](#license)
---

## Features
* Typed entity model for Home Assistant domains (`light`, `sensor`, `switch`, `media_player`, and more)
* Reactive updates over WebSocket events (`state_changed`)
* Kotlin Flow-based observation APIs
* Kotlin Multiplatform structure
* Compose integration module for JVM desktop UI
* Sample app with:
  * desktop Compose UI
  * terminal table with live status updates

## Supported Targets
* Multiplatform-ready architecture in `kortex-core` and `kortex-compose`

## Modules
* `kortex-core`
  * HA client, entities, configuration loading, observable startup APIs
* `kortex-compose`
  * Compose integration utilities/components over `kortex-core`
* `kortex-sample`
  * runnable examples (desktop + terminal)

## Quick Start
### Requirements
* JDK 17+ (JDK 21 recommended)
* Gradle Wrapper (`gradlew`, `gradlew.bat`)
* Home Assistant host + long-lived access token

### Configuration
Kortex loads config from `application.conf` under `kortex.*` with environment variable support.

Set the recommended variables:
* `HA_HOST` (example: `192.168.1.100`)
* `HA_PORT` (example: `8123`)
* `HA_TOKEN` (long-lived access token)

PowerShell:
```powershell
$env:HA_HOST = "192.168.1.100"
$env:HA_PORT = "8123"
$env:HA_TOKEN = "your-token"
```

### Run
From the repository root:
```powershell
cd %Source_Folder%\kortex
```

Run the desktop Compose sample (`com.ucasoft.kortex.sample.compose.MainKt`):
```powershell
.\gradlew :kortex-sample:jvmRun -DmainClass="com.ucasoft.kortex.sample.compose.MainKt"
```

Run terminal status table sample (`com.ucasoft.kortex.sample.MainKt`):
```powershell
.\gradlew :kortex-sample:jvmRun -DmainClass="com.ucasoft.kortex.sample.MainKt" --console=plain -q
```

Use `--console=plain` to prevent Gradle progress UI from injecting lines into live table output.

## Usage
### Observable Startup
```kotlin
import com.ucasoft.kortex.startKortexObservable

suspend fun main() {
    startKortexObservable { entities ->
        val lights = entities.lights
        println("Found lights: ${lights.size}")
    }
}
```

#### Live Terminal Status Table
The sample includes a terminal table printer that:
* shows `friendlyName` and `status`
* updates rows on `onToggled` events
* keeps the cursor below the table bottom after updates

For the best behavior, run in a real terminal (PowerShell or Windows Terminal), not IDE run output.

### Compose Startup
```kotlin
import com.ucasoft.kortex.startKortexCompose

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    state = rememberWindowState(width = 1024.dp, height = 768.dp)
  ) {
      KortexApplication(token, host) {

          val state = LocalKortexApplication.current

          when {
              state.error != null -> {
                  Text(state.error!!)
              }

              state.isLoading -> {
                  Box(
                      modifier = Modifier.fillMaxSize(),
                      contentAlignment = Alignment.Center
                  ) {
                      CircularProgressIndicator()
                  }
              }

              else -> {
                  HomeAssistant(state)
              }
          }
      }
  }
}
```

## License
Apache-2.0. See [LICENSE](LICENSE).
