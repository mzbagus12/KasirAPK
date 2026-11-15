plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

val signingPath = providers.environmentVariable("KASIR_KEYSTORE")
val signingAlias = providers.environmentVariable("KASIR_KEY_ALIAS")
val signingStorePassword = providers.environmentVariable("KASIR_STORE_PASSWORD")
val signingKeyPassword = providers.environmentVariable("KASIR_KEY_PASSWORD")

android {
    namespace = "com.apkkasir"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.apkkasir"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (signingPath.isPresent) {
        signingConfigs.create("kasirRelease") {
            storeFile = file(signingPath.get())
            keyAlias = signingAlias.get()
            storePassword = signingStorePassword.get()
            keyPassword = signingKeyPassword.get()
        }
    }
    buildTypes.named("release") {
        isDebuggable = false
        signingConfigs.findByName("kasirRelease")?.let { signingConfig = it }
    }

    buildFeatures { compose = true }
    sourceSets.getByName("androidTest").assets.srcDir("schemas")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { jvmToolchain(17) }

kapt { arguments { arg("room.schemaLocation", "$projectDir/schemas") } }

dependencies {
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
