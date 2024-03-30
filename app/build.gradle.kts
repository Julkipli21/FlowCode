plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.flowchart2code"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.flowchart2code"
        minSdk = 24
        targetSdk = 33
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:+")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

//    implementation("com.aallam.openai:openai-client:3.6.0")
//
//    platform("com.aallam.openai:openai-client-bom:3.6.0")
//
//    implementation("com.aallam.openai:openai-client")
//    runtimeOnly("io.ktor:ktor-client-okhttp")
//
//    implementation("io.insert-koin:koin-androidx-view-model:3.5.1")
//    implementation("io.insert-koin:koin-core:3.5.2-RC1")
//    implementation("io.insert-koin:koin-android:3.5.2-RC1")
//    implementation("io.insert-koin:koin-androidx-compose:3.5.2-RC1")
}