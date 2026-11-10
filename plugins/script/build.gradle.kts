plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":plugins:api"))
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.logging.jvm)
    compileOnly(libs.arrow.core)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.logback.classic)
    compileOnly(libs.swingx.all)
    compileOnly(libs.rsyntaxtextarea)

    // This module packaged ui-utils into its fat JAR in Maven as well
    // (the dependency had compile scope, unlike the other plugins).
    implementation(project(":ui-utils"))

    implementation(libs.groovy)
    implementation(libs.groovy.json)
    implementation(libs.groovy.sql)
    implementation(libs.groovy.xml)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk.jvm)
    testRuntimeOnly(libs.junit.platform.launcher)
}
