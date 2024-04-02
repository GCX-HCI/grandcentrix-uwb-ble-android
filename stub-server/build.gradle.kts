buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.springframework.cloud:spring-cloud-contract-gradle-plugin:3.0.3")
    }
}

plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.cloud.contract") version "3.0.3"
    kotlin("jvm") version "1.5.20-M1"
    kotlin("plugin.jpa") version "1.5.20-M1"
    kotlin("plugin.spring") version "1.5.20-M1"

    id("org.jlleitschuh.gradle.ktlint") version "$ktlint_version"
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-contract-dependencies:2.2.4.RELEASE")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:$jupiter_version")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
}

tasks.create<Copy>("copyContractsToSource") {
    from("${project.projectDir}/v1/")
    into("${project.projectDir}/src/test/resources/contracts/")
}

tasks.getByName("copyContracts").dependsOn.add("copyContractsToSource")

contracts {
    includedFiles.set(listOf("*.yaml"))
    testFramework.set(org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5)
}