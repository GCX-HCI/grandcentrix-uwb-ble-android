plugins {
    id("com.android.library")
    alias(libs.plugins.android.kotlin)
}

android {
    namespace = "gcx.test"
    compileSdk = libs.versions.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    api(libs.jupiter)
    api(libs.coroutines.test)
}