plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.plugin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    group = "com.ucasoft.kortex"

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    version = "0.0.1"
}