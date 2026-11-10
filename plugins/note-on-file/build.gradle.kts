plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":plugins:api"))
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.logging.jvm)
    compileOnly(libs.arrow.core)
    compileOnly(libs.rsyntaxtextarea)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.mockk.jvm)
    testRuntimeOnly(libs.junit.platform.launcher)
}
