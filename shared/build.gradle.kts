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

    // Configuration globale pour les binaires iOS (fix Xcode 16 + MapLibre)
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        if (konanTarget.family.isAppleFamily) {
            binaries.all {
                linkerOpts("-framework", "CoreLocation")
                // On désactive le cache pour éviter les erreurs de lien fantômes
                @Suppress("UNUSED_VARIABLE")
                val disableCache = (this as? org.jetbrains.kotlin.gradle.plugin.mpp.Framework)?.apply {
                    // Cette option est parfois nécessaire en cas de crash du linker
                }
                // Correction directe recommandée par le compilateur :
                freeCompilerArgs += "-Xdisable-native-cache"
            }
        }
    }

    cocoapods {
        summary = "Shared module for EssenceMaintenant"
        homepage = "https://github.com/dorian-bichet/EssenceMaintenant"
        version = "1.0"
        ios.deploymentTarget = "17.0"
        pod("MapLibre") {
            version = "~> 6.0"
        }
        framework {
            baseName = "Shared"
            isStatic = true
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

            // --- AJOUTS JALON 1 ---
            // Brique Réseau & Décodage (Ktor)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)

            // Brique Cartographie graphique (MapLibre)
            implementation(libs.maplibre.compose)

            // Sauvegarde de l'état à la fermeture de l'app
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