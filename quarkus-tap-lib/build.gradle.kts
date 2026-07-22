plugins {
    `java-library`
    `maven-publish`
    id("org.kordamp.gradle.jandex") version "1.1.0" //necessary to make quarkus look for beans
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(platform("org.javastro:bom:2026.2"))
    implementation(platform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("org.javastro.ivoa.core:common:0.1.0-SNAPSHOT")
    implementation("org.javastro.ivoa.core:tap:0.1.0-SNAPSHOT")
    implementation("io.quarkus:quarkus-rest")
    implementation("org.jspecify:jspecify:1.0.0")
}

group = "org.javastro.ivoa.core.quarkus"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("pahjbo")
                        name.set("Paul Harrison")
                        email.set("paul.harrison@manchester.ac.uk")
                    }
                }
            }
        }
    }
    repositories {
        maven { //Only publish here whilst developing initial versions - ultimately want maven central.
            name = "uksrcrepo"
            credentials {
                username = (findProperty("uksrcNexusUsername") ?: System.getenv("UKSRC_REPO_USERNAME")) as String?
                password = (findProperty("uksrcNexusPassword") ?: System.getenv("UKSRC_REPO_PASSWORD")) as String?
            }
            val releasesRepoUrl = uri("https://repo.dev.uksrc.org/repository/maven-releases/")
            val snapshotsRepoUrl = uri("https://repo.dev.uksrc.org/repository/maven-snapshots/")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
}