// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // reads app/google-services.json so the app knows which Firebase project to use
    alias(libs.plugins.google.services) apply false
}
