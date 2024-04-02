import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("java-library")
    id("kotlin")
    id("org.openapi.generator") version "5.3.0"
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/specs/api.yml")
    outputDir.set("$buildDir/generated")
    packageName.set("net.grandcentrix.scaffold.backend")

    templateDir.set("$projectDir/codegen-template")

    configOptions.set(
        mapOf(
            "library" to "jvm-okhttp4",
            "useCoroutines" to "true"
        )
    )

    globalProperties.set(
        mapOf(
            "modelDocs" to "false",
            "apiDocs" to "false"
        )
    )
}

tasks {
    named("compileKotlin") { dependsOn(openApiGenerate) }
}

kotlin {
    sourceSets {
        getByName("main").kotlin.srcDirs("$buildDir/generated/src/main/kotlin")
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    val versions = rootProject.extra
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", version = KotlinCompilerVersion.VERSION))

    implementation("com.squareup.okhttp3:okhttp:${versions["okhttp_version"]}")
    implementation("com.squareup.moshi:moshi-kotlin:${versions["moshi_version"]}")

    implementation("net.grandcentrix.either:either:${versions["either_version"]}")
}
