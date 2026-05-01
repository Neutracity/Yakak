
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kayak.yakak"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kayak.yakak"
        minSdk = 34
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "com.kayak.yakak.HiltTestRunner"
    }



    /*ksp {
        arg("hilt.enableMessagingDestinationsInventory", "false")
    }*/

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("org.osmdroid:osmdroid-android:6.1.18")//ajout openstreetmapinshalla
    implementation("com.kizitonwose.calendar:compose:2.10.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.protolite.well.known.types)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.coil.compose)
    implementation(libs.androidx.test.core)
    implementation(libs.androidx.rules)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.room.runtime)
    ksp("androidx.room:room-compiler:2.8.4")
    kspAndroidTest(libs.hilt.android.compiler)
    implementation(libs.androidx.room.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.hilt.android.testing)


    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // Tests dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.core)

    // Nécessaire pour tester les activités Compose isolées
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}