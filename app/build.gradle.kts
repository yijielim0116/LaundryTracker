import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    // Firebase setup comes from app/google-services.json (git ignores it, like local.properties)
    alias(libs.plugins.google.services)
}

// my Google Maps key lives in local.properties (git ignores that file) so it never ends up on GitHub
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "org.setu.laundrytracker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.setu.laundrytracker"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // puts the key into the manifest as ${MAPS_API_KEY}, empty if it's not set (the map just shows grey)
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Gson turns my machines into JSON text and back again
    implementation(libs.gson)
    // Material Components, I need it for the NavigationView in the navigation drawer
    implementation(libs.material)
    // Timber for logging, same as my PlacedMark lab (shows in Logcat)
    implementation(libs.timber)
    // Google Maps, for picking a machine's location and showing all machines on a map
    implementation(libs.play.services.maps)
    // Firebase, the BoM picks versions of the Firebase libraries that work together
    implementation(platform(libs.firebase.bom))
    // Firebase Authentication for sign up / login with email and password
    implementation(libs.firebase.auth)
    // Cloud Firestore, where each user's machines are saved now
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
