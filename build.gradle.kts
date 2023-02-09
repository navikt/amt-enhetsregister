import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

group = "no.nav.amt_enhetsregister"
version = "0.0.1-SNAPSHOT"
description = "ACL mot Brønnøysundregisteret"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { setUrl("https://packages.confluent.io/maven/") }
    maven { setUrl("https://jitpack.io") }
}

val commonVersion = "2023.02.08_14.02-e5f1a7a4b9e4"
val testcontainersVersion = "1.17.6"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("com.github.navikt.common-java-modules:job:$commonVersion") {
        exclude("com.github.navikt.common-java-modules", "log")
    }
    implementation("com.github.navikt.common-java-modules:rest:$commonVersion") {
        exclude("com.github.navikt.common-java-modules", "log")
    }
    implementation("com.github.navikt.common-java-modules:kafka:$commonVersion") {
        exclude("com.github.navikt.common-java-modules", "log")
    }

    implementation("org.flywaydb:flyway-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.31.0")
    implementation("net.javacrumbs.shedlock:shedlock-core:4.31.0")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    implementation("no.nav.security:token-validation-spring:3.0.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    runtimeOnly("org.postgresql:postgresql")
    
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("no.nav.security:mock-oauth2-server:0.3.5")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("com.vaadin.external.google", "android-json")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
