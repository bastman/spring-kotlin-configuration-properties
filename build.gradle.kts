import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {

    val KOTLIN_VERSION="1.3.21"

    java
    kotlin("jvm") version KOTLIN_VERSION
    application

    id("org.jetbrains.dokka") version "0.9.17" apply false
    // spring
    id("org.springframework.boot") version "2.1.2.RELEASE" apply true
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    // kotlin: spring (proxy) related plugins see: https://kotlinlang.org/docs/reference/compiler-plugins.html
    id("org.jetbrains.kotlin.plugin.spring") version KOTLIN_VERSION
    id("org.jetbrains.kotlin.plugin.noarg") version KOTLIN_VERSION
    id("org.jetbrains.kotlin.plugin.allopen") version KOTLIN_VERSION
}

group = "com.example"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "com.example.demo.MainKt"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}


dependencies {
    compile(kotlin("stdlib-jdk8"))
    // logging
    implementation("io.github.microutils:kotlin-logging:1.6.10")

    // serialization: jackson json
    val jacksonVersion =  "2.9.7"
    implementation("com.fasterxml.jackson.module:jackson-modules-java8:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    // jmespath ... you know "jq" ;)
    implementation("io.burt:jmespath-jackson:0.2.1")

    // spring
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group="org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
