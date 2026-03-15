plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.plugin.serialization)
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":kortex-core"))
                implementation(project(":kortex-compose"))
                implementation(libs.compose.material3)
                implementation(libs.compose.material.icons)
                implementation(kotlin("reflect"))
                implementation(libs.coil.compose)
                implementation(libs.coil.network.okhttp)
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}