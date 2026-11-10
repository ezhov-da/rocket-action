import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":plugins:api"))
    implementation(project(":utils"))
    implementation(project(":ui-utils"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.rsyntaxtextarea)
    implementation(libs.swingx.all)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.arrow.core)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.snakeyaml)
    implementation(libs.h2)
    implementation(libs.liquibase.core)
    implementation(libs.ktorm.core)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val liquibaseRuntime by configurations.creating

dependencies {
    liquibaseRuntime(libs.liquibase.core)
    liquibaseRuntime(libs.h2)
}

// Maven equivalent:
//   mvnw.cmd liquibase:update -Dliquibase.contexts=dev
// Gradle equivalent:
//   gradlew :application-core:liquibaseUpdate -Pliquibase.contexts=dev
//   gradlew :application-core:liquibaseUpdate "-Pliquibase.contexts=!dev"
tasks.register<JavaExec>("liquibaseUpdate") {
    group = "liquibase"
    description = "Runs Liquibase update against the H2 database configured in ../liquibase/liquibase.properties."
    classpath = liquibaseRuntime
    mainClass.set("liquibase.integration.commandline.Main")
    workingDir = projectDir

    val contexts = providers.gradleProperty("liquibase.contexts")
        .orElse(providers.systemProperty("liquibase.contexts"))

    val liquibaseArgs = mutableListOf(
        "--defaultsFile=../liquibase/liquibase.properties",
        "--changeLogFile=../liquibase/dbchangelog.xml",
    )
    if (contexts.isPresent) {
        liquibaseArgs.add("--contexts=${contexts.get()}")
    }
    args = liquibaseArgs
}

tasks.named<Jar>("fatJar") {
    // Keep the Maven assembly output name.
    archiveFileName.set("rocket-action-application-core-fat.jar")
}
