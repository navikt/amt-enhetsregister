plugins {
    val kotlinVersion = "2.2.21"

    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
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

val commonVersion = "3.2025.06.23_14.50-3af3985d8555"
val testcontainersVersion = "2.0.2"
val logstashEncoderVersion = "9.0"
val shedlockVersion = "7.2.1"
val tokenSupportVersion = "5.0.34"
val okHttpVersion = "5.3.2"
val mockOauth2ServerVersion = "3.0.1"
val mockkVersion = "1.14.7"

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

// fjernes ved neste release av org.apache.kafka:kafka-clients
configurations.configureEach {
    resolutionStrategy {
        capabilitiesResolution {
            withCapability("org.lz4:lz4-java") {
                select(candidates.first { (it.id as ModuleComponentIdentifier).group == "at.yawk.lz4" })
            }
        }
    }
}

dependencies {
    implementation("at.yawk.lz4:lz4-java:1.10.1") // fjernes ved neste release av org.apache.kafka:kafka-clients
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("no.nav.common:job:$commonVersion")
    implementation("no.nav.common:rest:$commonVersion")
    implementation("no.nav.common:kafka:$commonVersion") {
        exclude("org.apache.avro", "avro")
        exclude("org.xerial.snappy", "snappy-java")
        exclude("io.confluent", "kafka-avro-serializer")
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

    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("com.vaadin.external.google", "android-json")
    }
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property",
        )
    }
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "-Xshare:off",
        "-XX:+EnableDynamicAgentLoading",
    )
}
