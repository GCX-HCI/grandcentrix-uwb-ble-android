// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter {
            content {
                // Use jcenter only on an "allow" basis; use "include…" to include allowed packages/modules
            }
        }
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.3.1")
        classpath(kotlin("gradle-plugin", version = "1.5.31"))

        classpath("com.google.gms:google-services:4.3.15")

        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.6")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

allprojects {
    extra.apply {
        set("ktlint_version", "10.2.0")
        set("koin_version", "3.1.4")
        set("material_version", "1.3.0")
        set("appcompat_version", "1.4.0")
        set("constraintlayout_version", "2.1.2")
        set("datastore_version", "1.0.0")
        set("lifecycle_version", "2.4.0")
        set("navigation_components_version", "2.3.5")
        set("firebase_bom_version", "29.0.3")
        set("either_version", "1.7")
        set("auth0_version", "2.6.0")
        set("moshi_version", "1.12.0")
        set("okhttp_version", "4.9.3")
        set("glide_version", "4.12.0")
        set("jupiter_version", "5.7.2")
        set("compose_version", "1.0.5")
        set("navigation_compose_version", "2.4.1")
        set("accompanist_version", "0.11.1")
        set("androidx_test_junit_version", "1.1.3")
        set("androidx_test_espresso_version", "3.4.0")
    }

    repositories {
        maven("https://maven.pkg.github.com/GCX-HCI/grandcentrix-kotlin-either") {
            credentials {
                username =
                    project.findProperty("github.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password =
                    project.findProperty("github.token") as String? ?: System.getenv("GITHUB_PAT")
            }
            content {
                includeGroup("net.grandcentrix.either")
            }
        }
        google()
        jcenter {
            content {
                // Use jcenter only on an "allow" basis; use "include…" to include allowed packages/modules
            }
        }
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        filter {
            exclude("**/generated/**")
            exclude { element -> element.file.path.contains("generated/") }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
