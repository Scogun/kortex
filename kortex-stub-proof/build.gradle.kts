plugins {
    alias(libs.plugins.multiplatform)
}

kotlin {
    jvmToolchain(17)
    jvm()
    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":kortex-core"))
                implementation(libs.ktor.serialization.json)
            }
        }
    }
}
