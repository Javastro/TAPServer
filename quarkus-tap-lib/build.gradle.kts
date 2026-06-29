plugins {
    `java-library`
    id("org.kordamp.gradle.jandex") version "1.1.0" //necessary to make quarkus look for beans
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(platform("org.javastro:bom:2026.2"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("org.javastro.ivoa.core:common:0.1.0-SNAPSHOT")
    implementation("org.javastro.ivoa.core:tap:0.1.0-SNAPSHOT")
    implementation("io.quarkus:quarkus-rest")
    implementation("org.jspecify:jspecify:1.0.0")
}

group = "org.javastro.ivoa.servers"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
