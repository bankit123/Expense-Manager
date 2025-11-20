plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "trackmyspend.budgetplanner.expensemanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "trackmyspend.budgetplanner.expensemanager"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.1.4"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.google.android.material:material:1.13.0")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("androidx.activity:activity:1.9.2")
    implementation ("com.airbnb.android:lottie:6.1.0")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1") // for Java
    // (Optional) Room with RxJava2 / RxJava3 support
    implementation ("androidx.room:room-rxjava3:2.6.1")

    // Gson (if you need JSON parsing for extra data fields)
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.8.5")
    implementation ("androidx.lifecycle:lifecycle-runtime:2.8.5")

    implementation ("de.raphaelebner:roomdatabasebackup:1.1.0")
    implementation("com.airbnb.android:lottie:5.2.0")
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    implementation ("com.google.android.play:app-update:2.1.0")
    implementation ("com.google.android.play:app-update-ktx:2.1.0")
    implementation ("com.facebook.android:facebook-android-sdk:[8,9)")

}