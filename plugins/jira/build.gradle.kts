plugins {
    kotlin("jvm")
}

// Maven resolves these transitively through the Jira REST client; Gradle's
// highest-version strategy selects different versions.
configurations.all {
    resolutionStrategy {
        force(
            "org.apache.httpcomponents:httpcore:4.4.10",
            "jakarta.activation:jakarta.activation-api:1.2.2",
            "jakarta.xml.bind:jakarta.xml.bind-api:2.3.3",
        )
    }
}

dependencies {
    compileOnly(project(":plugins:api"))
    compileOnly(project(":ui-utils"))
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.logging.jvm)
    compileOnly(libs.arrow.core)
    compileOnly(libs.swingx.all)

    implementation(libs.jira.rest.java.client.core)
    implementation(libs.fugue)

    // The jersey-media-json-jettison POM adds jakarta.xml.bind-api via a
    // "jdk11+" profile, which Maven activates on JDK 11+ but Gradle ignores.
    // Declare it explicitly to keep the runtime classpath identical.
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:2.3.3")

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk.jvm)
    testRuntimeOnly(libs.junit.platform.launcher)
}
