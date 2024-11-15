plugins {
    id("java")
    id ("me.champeau.jmh") version "0.7.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

group = "de.notizwerk.async"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.h2database:h2:2.2.224")
    "jmh"("org.openjdk.jmh:jmh-core:1.37")
    "jmh"("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    iterations.set(3)
    warmupIterations.set(2)
    fork.set(1)
    failOnError.set(true)
    resultFormat.set("JSON")
    // Ergebnisse werden in build/results/jmh gespeichert
    resultsFile.set(project.file("${layout.buildDirectory.get()}/results/jmh/results.json"))
}

tasks.test {
    useJUnitPlatform()
}

