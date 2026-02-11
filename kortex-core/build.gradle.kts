plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.plugin.serialization)
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.config)
                implementation(libs.koin.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                api(libs.ktor.client.websockets)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}