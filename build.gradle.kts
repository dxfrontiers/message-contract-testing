buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
    }

    dependencies {
        classpath("org.springframework.cloud:spring-cloud-contract-gradle-plugin:2.2.0.RELEASE")
        classpath("org.springframework.cloud:spring-cloud-contract-spec-kotlin:2.2.0.RELEASE")
        classpath("org.jetbrains.kotlin:kotlin-script-util:1.3.61")
    }
}

plugins {
    id("org.springframework.boot") version "2.2.1.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.8.RELEASE" apply false
    kotlin("jvm") version "1.3.61" apply false
    kotlin("plugin.spring") version "1.3.61" apply false
    id("com.github.ben-manes.versions") version "0.27.0" apply false
}

subprojects {
    group = "de.digitalfrontiers"
    version = "0.0.1-SNAPSHOT"
}
