plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":plugins:api"))
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.logging.jvm)
    compileOnly(libs.arrow.core)

    implementation(libs.egit.github.core)
}
