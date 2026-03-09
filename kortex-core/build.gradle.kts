plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.plugin.serialization)
}

kotlin {
    jvm {
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
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
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.server.cio)
                implementation(libs.koin.test)
                implementation(libs.kotest.assertions.core)
            }
            kotlin.srcDir("src/test/kotlin")
        }
    }
}