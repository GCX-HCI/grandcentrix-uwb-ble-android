import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

android {
    compileSdk = AppConfig.compileSdk
    buildToolsVersion = AppConfig.buildToolsVersion

    defaultConfig {
        applicationId = "net.grandcentrix.scaffold.app"
        versionCode = 1
        versionName = "1.0"

        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        addManifestPlaceholders(
            mapOf(
                "auth0Domain" to "@string/com_auth0_domain",
                "auth0Scheme" to "@string/com_auth0_domain_scheme"
            )
        )
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "${rootProject.extra["compose_version"]}"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {

    val versions = rootProject.extra

    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation(
        "com.google.android.material:material:${versions["material_version"]}"
    )

    implementation(
        "androidx.appcompat:appcompat:${versions["appcompat_version"]}"
    )
    implementation(
        "androidx.constraintlayout:constraintlayout:${versions["constraintlayout_version"]}"
    )
    implementation(
        "androidx.datastore:datastore-preferences:${versions["datastore_version"]}"
    )
    implementation(
        "androidx.lifecycle:lifecycle-livedata-ktx:${versions["lifecycle_version"]}"
    )
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${versions["lifecycle_version"]}"
    )
    implementation(
        "androidx.navigation:navigation-fragment-ktx:${versions["navigation_components_version"]}"
    )
    implementation(
        "androidx.navigation:navigation-ui-ktx:${versions["navigation_components_version"]}"
    )

    implementation("androidx.compose.ui:ui:${versions["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling:${versions["compose_version"]}")
    implementation("androidx.compose.foundation:foundation:${versions["compose_version"]}")
    implementation("androidx.compose.material:material:${versions["compose_version"]}")
    implementation(
        "androidx.navigation:navigation-compose:${versions["navigation_compose_version"]}"
    )
    implementation("com.google.accompanist:accompanist-glide:${versions["accompanist_version"]}")

    // Use Firebase Bill Of Materials to combine compatible versions
    // https://firebase.google.com/docs/android/learn-more#bom
    implementation(platform("com.google.firebase:firebase-bom:${versions["firebase_bom_version"]}"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("io.insert-koin:koin-android:${versions["koin_version"]}")
    implementation("io.insert-koin:koin-androidx-compose:${versions["koin_version"]}")

    implementation("net.grandcentrix.either:either:${versions["either_version"]}")

    implementation("com.auth0.android:auth0:${versions["auth0_version"]}")

    implementation("com.github.bumptech.glide:glide:${versions["glide_version"]}")
    kapt("com.github.bumptech.glide:compiler:${versions["glide_version"]}")

    implementation("com.squareup.okhttp3:okhttp:${versions["okhttp_version"]}")

    implementation(project(":api-backend"))
    implementation("com.squareup.okhttp3:logging-interceptor:${versions["okhttp_version"]}")

    testImplementation("org.junit.jupiter:junit-jupiter:${versions["jupiter_version"]}")

    androidTestImplementation("androidx.test.ext:junit:${versions["androidx_test_junit_version"]}")
    androidTestImplementation(
        "androidx.test.espresso:espresso-core:${versions["androidx_test_espresso_version"]}"
    )
}

// TODO Remove once Gradle 7 is in use and AS Arctic Fox has been released as stable
// See https://stackoverflow.com/a/61162647/15521204 for further information
android.sourceSets.all {
    java.srcDir("src/$name/kotlin")
}
