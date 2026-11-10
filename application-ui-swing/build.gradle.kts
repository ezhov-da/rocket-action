import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

// Maven resolves Jackson with "nearest wins": the versions declared in this POM
// (2.18.5) beat the higher versions requested by mcp-json-jackson2 (2.21.1) and
// swagger-core (2.13.3). Gradle picks the highest version instead, which would
// put Jackson 2.21 on the classpath; it is built against Kotlin 2.x and breaks
// the Kotlin 1.8 compilation. Pin exactly what Maven resolves.
configurations.all {
    resolutionStrategy {
        force(
            "com.fasterxml.jackson.core:jackson-annotations:2.18.5",
            "com.fasterxml.jackson.core:jackson-core:2.18.5",
            "com.fasterxml.jackson.core:jackson-databind:2.18.5",
            "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.5",
            "com.fasterxml.jackson.module:jackson-module-kotlin:2.18.5",
            // com.fasterxml.jackson.dataformat:jackson-dataformat-yaml comes
            // through swagger-core, jackson-dataformat-xml:2.18.2 through KeePassJava2.
            // YAML is aligned with the other Jackson modules because Spring Boot
            // brings SnakeYAML 2.x, which is incompatible with Jackson YAML 2.13.
            "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.5",
            "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.2",
            "org.jetbrains.kotlin:kotlin-reflect:1.8.10",
            // Declared with an explicit 31.1-jre version in the module POM, which
            // wins over the 32.x version requested transitively by swagger-core.
            "com.google.guava:guava:31.1-jre",
            // Maven resolves the JAXB/activation APIs through swagger-core.
            "jakarta.activation:jakarta.activation-api:1.2.1",
            "jakarta.xml.bind:jakarta.xml.bind-api:2.3.2",
        )
    }
}

dependencies {
    implementation(project(":plugins:api"))
    implementation(project(":utils"))
    implementation(project(":ui-utils"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.miglayout)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.guava)
    implementation(libs.rsyntaxtextarea)
    implementation(libs.swingx.all)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.arrow.core)
    implementation(libs.slf4j.api)
    implementation(libs.mustache.compiler)
    implementation(libs.groovy)
    implementation(libs.groovy.json)
    implementation(libs.groovy.sql)
    implementation(libs.groovy.swing)
    implementation(libs.groovy.xml)
    implementation(libs.lucene.core)
    implementation(libs.lucene.queryparser)
    implementation(libs.lucene.analyzers.common)
    implementation(libs.logback.classic)
    implementation(libs.snakeyaml)
    implementation(libs.flatlaf)
    implementation(libs.flatlaf.swingx)
    implementation(libs.swagger.core)
    implementation(libs.webp.imageio)
    implementation(libs.java.image.scaling)
    implementation(libs.commonmark)
    implementation(libs.commonmark.ext.gfm.tables)
    implementation(libs.jkeymaster)
    implementation(libs.quartz)
    implementation(libs.sqlite.jdbc)
    implementation(libs.keepassjava2)
    implementation(libs.mcp.core)
    implementation(libs.mcp.json.jackson2)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk.jvm)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// The application is packaged by Spring Boot. Its boot jar keeps the
// dependencies as nested jars, so every library keeps its own
// META-INF/spring.factories and AutoConfiguration.imports. A hand-merged fat
// jar keeps only one file per path and loses Spring Boot's own metadata
// (context factories, config-data post-processors, run listeners).
tasks.named<BootJar>("bootJar") {
    // Maven assembly produced target/rocket-action.jar (finalName = project.name).
    archiveFileName.set("rocket-action.jar")
    mainClass.set("ru.ezhov.rocket.action.application.AppKt")
}

// The plain library jar and the generic merged fat jar are replaced by the
// Boot jar for this module.
tasks.named<Jar>("jar") { enabled = false }
tasks.named<Jar>("fatJar") { enabled = false }

// Convenience task for local development.
// Run with: gradlew :application-ui-swing:run
tasks.register<JavaExec>("run") {
    group = "application"
    description = "Runs the Swing application with the project directory as working directory."
    mainClass.set("ru.ezhov.rocket.action.application.AppKt")
    classpath = sourceSets.main.get().runtimeClasspath
    workingDir = rootProject.projectDir
    standardInput = System.`in`
}

tasks.named<Test>("test") {
    // BuildIconsTest is a generator, not a test: it rewrites
    // src/main/resources/icons/icons.json from the PNG files.
    exclude("**/BuildIconsTest.class")
}
