import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    kotlin("native.cocoapods")
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidLibrary {
        namespace = "com.dorian_bichet.essence_maintenant.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    iosArm64()
    iosSimulatorArm64()

    // Configuration du Linker pour Xcode 16 + MapLibre
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.all {
            linkerOpts(
                "-framework", "CoreLocation",
                "-framework", "QuartzCore",
                "-L/usr/lib/swift",
                "-L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphonesimulator",
                "-L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift-5.0/iphonesimulator"
            )
        }
    }

    cocoapods {
        summary = "Shared module for EssenceMaintenant"
        homepage = "https://github.com/dorian-bichet/EssenceMaintenant"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        
        // 1. LIEN CRUCIAL : Indique à Gradle où trouver les Pods installés
        podfile = project.file("../iosApp/Podfile")

        pod("MapLibre") {
            version = "~> 6.0"
        }
        
        framework {
            baseName = "Shared"
            // 2. PASSAGE EN DYNAMIQUE : Plus robuste pour les dépendances Apple
            isStatic = false
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.maplibre.compose)

            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
