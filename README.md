# Kortex

**Kortex** is a Kotlin Multiplatform client toolkit for Home Assistant.
It provides typed entities, reactive state streams, and Compose integration for building automation apps and tools.

![GitHub](https://img.shields.io/github/license/Scogun/kortex?color=blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue)
![Ktor](https://img.shields.io/badge/Ktor-3.4.1-blue)
![Compose](https://img.shields.io/badge/Compose-1.10.2-blue)

---
* [Features](#features)
* [Project Status](#project-status)
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
* Typed entity model for Home Assistant domains (`light`, `sensor`, `switch`, `media_player`, `calendar`, and more)
* Reactive updates over WebSocket events (`state_changed`)
* Kotlin Flow-based observation APIs
* Kotlin Multiplatform modules
* Compose Multiplatform integration module
* Sample app with:
  * desktop Compose UI
  * terminal table with live status updates

## Project Status
* Current version: `0.0.1`
* Multi-module Gradle project:
  * `:kortex-core`
  * `:kortex-compose`
  * `:kortex-sample`

## Modules
* `kortex-core`
  * Home Assistant client, entities, configuration loading, and observable startup APIs
  * Targets: `jvm`, `linuxX64`, `linuxArm64`, `mingwX64`, `macosX64`, `macosArm64`, `iosX64`, `iosArm64`, `iosSimulatorArm64`, `js(IR)` (`browser`, `nodejs`)
* `kortex-compose`
  * Compose integration utilities/components over `kortex-core`
  * Targets: `jvm`, `macosX64`, `macosArm64`, `iosX64`, `iosArm64`, `iosSimulatorArm64`, `js(IR)` (`browser`, `nodejs`)
* `kortex-sample`
  * Runnable JVM sample apps (desktop Compose + terminal)

## Quick Start
### Requirements
* JDK 17+
* Gradle Wrapper (`gradlew`, `gradlew.bat`)
* Home Assistant host + long-lived access token

### Configuration
Kortex loads config from `application.conf` under `kortex.*` with environment variable support.
At runtime, values can be provided either through environment variables or directly in the config file.

Set the recommended variables:
* `HA_HOST` (example: `homeassistant.local` or `192.168.1.100`)
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
Set-Location %Source_Folder%\kortex
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

Optional Compose hot reload run task:
```powershell
.\gradlew :kortex-sample:hotRunJvm --mainClass=com.ucasoft.kortex.sample.compose.MainKt
```

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
import com.ucasoft.kortex.compose.KortexApplication

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
