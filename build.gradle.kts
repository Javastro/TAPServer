plugins {
    id("org.javastro.build.module.quarkus-app")
}

dependencies {
    implementation("org.javastro.ivoa.core.quarkus:quarkus-tap-lib:0.9.2")
    implementation("org.javastro.ivoa.core.quarkus:quarkus-uws:0.9.0")
    implementation("org.javastro.ivoa.core:dal:0.9.3")
    implementation("org.javastro.ivoa.core:uws:0.9.4-SNAPSHOT")

    implementation("org.javastro.ivoa.core:pgsphere:0.9.2")
    implementation("org.javastro.ivoa.dm:DemoDM:0.2")
    implementation("org.javastro.ivoa.dm:DemoDM:0.2:test")
    implementation("io.quarkus:quarkus-rest-jaxb")
    implementation("net.sf.saxon:Saxon-HE:12.5")
    implementation("io.quarkus:quarkus-rest-jsonb")
    implementation("io.quarkus:quarkus-smallrye-openapi")
//    implementation("io.quarkus:quarkus-reactive-pg-client")  // reactive client does not inject entitymanager
//    implementation("io.quarkus:quarkus-security-jpa-reactive")
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.awaitility:awaitility:4.3.0")

    constraints {
        implementation("org.javastro.ivoa:ivoa-entities:0.9.15") {
            because("previous version had h2db in dependencies, which is not needed and causes problems with quarkus native-executable")
        }


    }

}

group = "org.javastro.ivoa.servers"





tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")

}

tasks.named<Javadoc>("javadoc") {
    exclude("org/javastro/ivoa/tap/entities/**")
}

// should not be needed https://github.com/quarkusio/quarkus/discussions/40427 https://github.com/quarkusio/quarkus/issues/38996
//tasks.named<JavaCompile>("compileJava") {
//    dependsOn(tasks.named("compileQuarkusGeneratedSourcesJava"))
//}
