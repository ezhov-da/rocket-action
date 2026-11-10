plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.logging.jvm)
    compileOnly(libs.arrow.core)
    compileOnly(libs.rsyntaxtextarea)
    compileOnly(project(":utils"))
}
