plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.plugin.serialization)
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                api(libs.koin.core)
                implementation(libs.config)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.websockets)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}