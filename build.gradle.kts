import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "2.0.0"

    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

group = "no.nav.amt_enhetsregister"
version = "0.0.1-SNAPSHOT"
description = "ACL mot Brønnøysundregisteret"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven { setUrl("https://packages.confluent.io/maven/") }
    maven { setUrl("https://github-package-registry-mirror.gc.nav.no/cached/maven-release") }
}

val commonVersion = "3.2024.05.23_05.46-2b29fa343e8e"
val testcontainersVersion = "1.20.0"
val logstashEncoderVersion = "7.4"
val shedlockVersion = "5.13.0"
val tokenSupportVersion = "5.0.1"
val okHttpVersion = "4.12.0"
val mockOauth2ServerVersion = "2.1.8"
val mockkVersion = "1.13.11"

extra["postgresql.version"] = "42.7.2"
extra["jackson-bom.version"] = "2.17.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("no.nav.common:job:$commonVersion")
    implementation("no.nav.common:rest:$commonVersion")
    implementation("no.nav.common:kafka:$commonVersion") {
        exclude("org.apache.avro", "avro")
        exclude("org.xerial.snappy", "snappy-java")
        exclude("io.confluent","kafka-avro-serializer")
    }

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-core:$shedlockVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("com.vaadin.external.google", "android-json")
    }
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
