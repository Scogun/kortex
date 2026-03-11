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
                api(libs.koin.compose)
                implementation(project(":kortex-core"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}