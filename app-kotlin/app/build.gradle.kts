plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    id("com.google.devtools.ksp") version "1.9.0-1.0.11"  // Versión compatible de KSP
}

kotlin {
    jvmToolchain(17) // Configura correctamente Kotlin a la versión JVM 17
}

android {
    namespace = "com.example.focus_planner"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.focus_planner"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17  // Cambia a Java 21
        targetCompatibility = JavaVersion.VERSION_17  // Cambia a Java 21
    }

    kotlinOptions {
        jvmTarget = "17"  // Asegúrate de que el target de Kotlin sea también Java 21
    }
    buildFeatures {
        compose = true
        viewBinding = true // Aquí habilitas View Binding
    }
}

dependencies {
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.appcompat)
    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.03.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material 3
    implementation(libs.material3)

    // ViewModel y LiveData
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Retrofit y Gson para la API
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Navegación en Jetpack Compose
    implementation(libs.androidx.navigation.compose)

    // Coil (para cargar imágenes)
    implementation(libs.coil.compose)

    // Room
    implementation(libs.androidx.room.runtime)  // Asegúrate de que la versión sea correcta
    annotationProcessor(libs.androidx.room.compiler)  // Si usas Java
//    ksp("androidx.room:room-compiler:2.6.1")  // Si usas Kotlin

    // Room para coroutines
    implementation(libs.androidx.room.ktx)  // Asegúrate de que la versión sea correcta

    //Calendario
    implementation (libs.jakewharton.threetenabp)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
