plugins {
    id("com.android.library")
    alias(libs.plugins.android.kotlin)
    id("maven-publish")
}

android {
    namespace = "net.grandcentrix.lib"
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

    implementation(libs.bundles.coroutines)
    implementation(libs.uwb)
    testImplementation(libs.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}

group = "net.grandcentrix.lib"
version = "0.0.2"

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            artifactId = "uwb"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/GCX-HCI/grandcentrix-uwb-ble-android")
            credentials {
                username = project.findProperty("github.user")?.toString()
                    ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("github.token")?.toString()
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
