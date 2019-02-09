import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.21"
    application
    id("org.springframework.boot") version "2.1.2.RELEASE" apply false
    id("org.jetbrains.dokka") version "0.9.17" apply false
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
}

group = "com.example"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "com.example.App"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}


dependencies {
    compile(kotlin("stdlib-jdk8"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
