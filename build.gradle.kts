@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
}

group = "hu.playground"
version = "1.0.0-SNAPSHOT"

sourceSets["main"].java.srcDir("$projectDir/src/main/java")
java.sourceCompatibility = JavaVersion.VERSION_11
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

dependencies {
    // KOTLIN DEPENDENCIES
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("net.jcip:jcip-annotations:1.0")

    // TEST DEPENDENCIES
    testImplementation("io.mockk:mockk:1.9.2")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "$group"
            version = "$version"

            from(components["java"])
        }
    }
}

repositories {
    maven {
        url = URI(project.findProperty("artifactory_resolveUrl") as? String)
        credentials {
            username = project.findProperty("artifactory_user") as String?
            password = project.findProperty("artifactory_password") as String?
        }
    }
    mavenLocal()
    mavenCentral()
}
