plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":plugins:api"))
    compileOnly(project(":ui-utils"))
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.logging.jvm)
    compileOnly(libs.arrow.core)

    implementation(libs.batik.swing)
}
