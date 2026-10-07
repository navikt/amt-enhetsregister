import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    val kotlinVersion = "2.4.20"

    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

group = "no.nav.amt_enhetsregister"
version = "0.0.1-SNAPSHOT"
description = "ACL mot Brønnøysundregisteret"

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

val jacksonModuleKotlinVersion = "3.2.3"
val commonVersion = "4.2026.09.24_06.17-80dfc0eacb29"
val logstashEncoderVersion = "9.0"
val shedlockVersion = "7.10.1"
val tokenSupportVersion = "6.0.13"
val okHttpVersion = "5.5.0"
val mockOauth2ServerVersion = "6.0.4"
val mockkVersion = "1.14.11"

// Override Spring Boot's managed Jackson versions to apply the security fixes in 3.1.7.
dependencyManagement {
    dependencies {
        dependency("tools.jackson.core:jackson-core:3.1.7")
        dependency("tools.jackson.core:jackson-databind:3.1.7")
    }
}

dependencies {
    constraints {
        implementation("at.yawk.lz4:lz4-java") {
            version { strictly("1.11.2") }
            because("Fixes CVE-2026-59949")
        }
    }

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    runtimeOnly("org.springframework.boot:spring-boot-starter-jetty")
    runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    runtimeOnly("org.springframework.boot:spring-boot-flyway")

    implementation("tools.jackson.module:jackson-module-kotlin:${jacksonModuleKotlinVersion}")

    implementation("no.nav.common:job:$commonVersion")
    implementation("no.nav.common:rest:$commonVersion")
    implementation("no.nav.common:kafka:$commonVersion") {
        exclude("org.apache.avro", "avro")
        exclude("org.xerial.snappy", "snappy-java")
        exclude("io.confluent", "kafka-avro-serializer")
    }

    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")

    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-kafka")

    testImplementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property",
        )
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs(
        "-Xshare:off",
        "-XX:+EnableDynamicAgentLoading",
    )
}
