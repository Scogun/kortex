plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.plugin.serialization)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(17)
    jvm {
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()
    js(IR) {
        browser()
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain {
            dependencies {
                api(libs.koin.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.websockets)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.server.cio)
                implementation(libs.koin.test)
                implementation(libs.kotest.assertions.core)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.config)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        configurePom("Kortex Core", "Kotlin Multiplatform toolkit for Home Assistant", this)
    }
}
