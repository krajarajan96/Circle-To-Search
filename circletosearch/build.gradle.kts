import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

group = "com.circletosearch"
version = "0.1.0"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CircleToSearch"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.mlkit.text.recognition)
            implementation(libs.kotlinx.coroutines.play.services)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}

android {
    namespace = "com.circletosearch"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Circle To Search")
            description.set(
                "Compose Multiplatform SDK for a Google Lens-style circle-to-search gesture, " +
                    "with pluggable OCR and theming.",
            )
            url.set("https://github.com/krajarajan96/Circle-To-Search")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://github.com/krajarajan96/Circle-To-Search/blob/main/LICENSE")
                }
            }
        }
    }
}
