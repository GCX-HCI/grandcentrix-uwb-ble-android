plugins {
    id("com.android.library")
    alias(libs.plugins.android.kotlin)
}

android {
    namespace = "net.grandcentrix.data"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {

    implementation(libs.bundles.koin)
    implementation(libs.bundles.coroutines)
    implementation(libs.uwb)
    implementation(project(":core:ble"))
    implementation(project(":core:uwb"))

    testImplementation(libs.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
