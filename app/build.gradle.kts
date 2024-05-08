import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.firmador"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.firmador"
        minSdk = 33
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding=true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation ("com.github.gcacace:signature-pad:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")
    implementation("com.itextpdf:itextg:5.5.10")
    implementation("com.opencsv:opencsv:5.5.2")

    //implementation("com.hierynomus:smbj:0.11.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}
