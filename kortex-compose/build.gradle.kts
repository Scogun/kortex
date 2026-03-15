plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(17)
    jvm()
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
                api(libs.koin.compose)
                implementation(project(":kortex-core"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        configurePom("Kortex Compose", "Compose Multiplatform integration utilities and components over Kortex", this)
    }
}
