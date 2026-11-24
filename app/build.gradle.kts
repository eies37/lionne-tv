import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.lionne.tv"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lionne.tv"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    // Signature release : activée seulement si la clé est fournie (secrets GitHub ou variables locales).
    val keystoreFile = System.getenv("LIONNE_KEYSTORE_FILE")
    if (keystoreFile != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("LIONNE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("LIONNE_KEY_ALIAS")
                keyPassword = System.getenv("LIONNE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (keystoreFile != null) signingConfig = signingConfigs.getByName("release")
            // Application la plus légère possible : les TV ont peu de stockage.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        // Analyse aussi tous les modules dont dépend l'application.
        checkDependencies = true
        abortOnError = true
        warningsAsErrors = false
        // Versions des bibliothèques : suivies à part, pas une erreur de code.
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable", "OldTargetApi")
        textReport = true
        textOutput = file("stdout")
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    // Socle
    implementation(project(":core:data"))
    implementation(project(":core:storage"))
    implementation(project(":core:designsystem"))

    // Sources de contenu : retirer une ligne ici (et dans AppContainer) suffit à désactiver un format.
    implementation(project(":source:jsoncatalog"))
    implementation(project(":source:m3u"))
    implementation(project(":source:xmltv"))

    // Écrans
    implementation(project(":feature:home"))
    implementation(project(":feature:player"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Images (logos, affiches) : chargement réseau et cache disque borné.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
