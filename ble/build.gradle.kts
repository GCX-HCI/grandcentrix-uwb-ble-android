@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("com.android.library")
    alias(libs.plugins.android.kotlin)
}

android {
    namespace = "gcx.ble"
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
    implementation(libs.arrow)
    implementation(libs.bundles.coroutines)

    testImplementation(libs.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}