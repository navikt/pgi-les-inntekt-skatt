import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val maskinportenClientVersion = "0.1.0"
val joseJwtVersion = "9.40"
// val kafkaVersion = "3.8.0"
val pgiDomainVersion = "0.0.5"
val micrometerVersion = "1.13.4"
val logbackVersion = "1.4.14"
val logstashVersion = "5.2"
val slf4jVersion = "2.0.9"
val log4jVersion = "2.20.0"
// spring-dependency-management krever denne, roter det til med 5.11
val junitJupiterVersion = "5.10.3"
val assertJVersion = "3.26.3"
val wiremockVersion = "3.9.1"
val mockkVerion = "1.13.12"
val springBootVersion = "3.3.3"
val springKafkaTestVersion = "3.2.3"

// overstyrte transitive avhengigheter
val guavaVersion = "33.3.0-jre"
val snappyJavaVersion = "1.1.10.6"
// val snakeYamlVersion = "2.2"
val commonsCompressVersion = "1.26.0"

// påkrevd av pgi-domain
val jacksonVersion = "2.17.2"

val jerseyVersion = "3.1.8"


group = "no.nav.pgi"

plugins {
    val kotlinVersion = "2.0.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.springframework.boot") version "3.3.2"
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("com.github.ben-manes.versions") version "0.51.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

apply(plugin = "io.spring.dependency-management")

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
    maven("https://maven.pkg.github.com/navikt/pensjon-samhandling-ktor-support") {
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    maven("https://maven.pkg.github.com/navikt/pgi-domain") {
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    maven("https://maven.pkg.github.com/navikt/pensjon-opptjening-gcp-maskinporten-client") {
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    // påkrevd av pgi-domain
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("no.nav.pensjonopptjening:pensjon-opptjening-gcp-maskinporten-client:$maskinportenClientVersion")
    implementation("com.nimbusds:nimbus-jose-jwt:$joseJwtVersion")

    implementation("org.apache.kafka:kafka-streams")
    implementation("org.apache.kafka:kafka-clients")

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    //    testImplementation("org.apache.kafka:kafka_2.13:$kafkaVersion")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("no.nav.pgi:pgi-domain:$pgiDomainVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    // Overstyrer noen transitive avhengigheter (pga sikkerhet m.m.)
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.xerial.snappy:snappy-java:$snappyJavaVersion")
//    implementation("org.yaml:snakeyaml:$snakeYamlVersion")
    implementation("org.apache.commons:commons-compress:$commonsCompressVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.kafka:spring-kafka-test:$springKafkaTestVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(("org.glassfish.jersey.core:jersey-server:$jerseyVersion"))
    testImplementation(("org.glassfish.jersey.core:jersey-common:$jerseyVersion"))
    testImplementation(("org.glassfish.jersey.core:jersey-client:$jerseyVersion"))
    testImplementation(("org.glassfish.jersey.inject:jersey-hk2:$jerseyVersion"))
    testImplementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")

    testImplementation("org.apache.kafka:kafka-streams-test-utils")
    testImplementation("io.mockk:mockk:$mockkVerion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = FULL
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.10.1"
}