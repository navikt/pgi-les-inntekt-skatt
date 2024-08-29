import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorSupportVersion = "0.0.22"
val ktorVersion = "1.5.4"
val maskinportenClientVersion = "0.1.0"
val joseJwtVersion = "9.40"
val kafkaVersion = "3.8.0"
val confluentVersion = "5.5.1"
val pgiDomainVersion = "0.0.5"
val micrometerVersion = "1.11.4"
val logbackVersion = "1.4.11"
val logstashVersion = "5.2"
val slf4jVersion = "2.0.9"
val log4jVersion = "2.20.0"
val junitJupiterVersion = "5.11.0"
val assertJVersion = "3.26.3"
val wiremockVersion = "2.27.2"
val mockkVerion = "1.13.12"
// val springBootVersion = "3.3.3"
// val springKafkaTestVersion = "3.2.3"

// overstyrte transitive avhengigheter
val guavaVersion = "32.1.2-jre"
val snappyJavaVersion = "1.1.10.5"
val snakeYamlVersion = "2.2"
val commonsCompressVersion = "1.24.0"

// påkrevd av pgi-domain
val jacksonVersion = "2.17.2"


group = "no.nav.pgi"

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.github.ben-manes.versions") version "0.51.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

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
// påkrevd av pgi-domain
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")


    // TODO: Disse var avhengigheter fra confluent-avro
    implementation("org.glassfish.jersey.core:jersey-common:2.30")

    implementation("no.nav.pensjonsamhandling:pensjon-samhandling-ktor-support:$ktorSupportVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")

    implementation("no.nav.pensjonopptjening:pensjon-opptjening-gcp-maskinporten-client:$maskinportenClientVersion")
    implementation("com.nimbusds:nimbus-jose-jwt:$joseJwtVersion")

    implementation("org.apache.kafka:kafka-streams:$kafkaVersion")
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

/*
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.kafka:spring-kafka-test:$springKafkaTestVersion")
    testImplementation("org.apache.kafka:kafka_2.13:$kafkaVersion")
*/

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
    implementation("org.yaml:snakeyaml:$snakeYamlVersion")
    implementation("org.apache.commons:commons-compress:$commonsCompressVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("com.github.tomakehurst:wiremock:$wiremockVersion")
    /*
    testImplementation("no.nav:kafka-embedded-env:$kafkaEmbeddedEnvVersion") {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
     */
    testImplementation("org.apache.kafka:kafka-streams-test-utils:$kafkaVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation("io.mockk:mockk:$mockkVerion")
    implementation(kotlin("stdlib"))

    /*
    // tvinger scala-versjoner pga avhengighetsrot i embedded-env, jackson osv
    implementation("org.scala-lang:scala-library:$scalaVersion")
    implementation("org.scala-lang:scala-reflect:$scalaVersion")
    testImplementation("org.scala-lang:scala-library:$scalaVersion")
    testImplementation("org.scala-lang:scala-reflect:$scalaVersion")
     */
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("app")

    manifest {
        attributes["Main-Class"] = "no.nav.pgi.skatt.inntekt.ApplicationKt"
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
        }
    }

    doLast {
        configurations.runtimeClasspath.get().forEach {
            val file = File("$buildDir/libs/${it.name}")
            if (!file.exists())
                it.copyTo(file)
        }
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
    gradleVersion = "8.10"
}
