plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":kortex-core"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.koin.compose)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}