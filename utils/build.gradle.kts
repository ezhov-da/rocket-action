plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.logging.jvm)
    compileOnly(libs.arrow.core)
    compileOnly(libs.jackson.databind)
    compileOnly(libs.jackson.datatype.jsr310)
    compileOnly(libs.jackson.module.kotlin)
    compileOnly(libs.jackson.dataformat.yaml)
    compileOnly(libs.snakeyaml)
    compileOnly(libs.commonmark)
    compileOnly(libs.commonmark.ext.gfm.tables)

    implementation(libs.jsoup)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}
