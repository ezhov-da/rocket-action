import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10" apply false
    kotlin("plugin.spring") version "1.8.10" apply false
    // The application module is packaged with the Spring Boot plugin: its
    // bootJar keeps the nested dependencies with their own META-INF metadata.
    alias(libs.plugins.spring.boot) apply false
}

group = "ru.ezhov"
version = "0.0.1"

val pluginApiVersion = "0.22.0"

// Module versions are kept exactly as in the Maven POMs. Keys are Gradle
// project paths, which keeps plugin subprojects of ":plugins" unambiguous.
val moduleVersions = mapOf(
    ":application-core" to "0.20.0",
    ":utils" to "0.20.0",
    ":ui-utils" to "0.0.3",
    ":plugins:api" to pluginApiVersion,
    ":plugins:note-on-file" to "${pluginApiVersion}_4",
    ":plugins:copy-to-clipboard" to "${pluginApiVersion}_1",
    ":plugins:exec" to "${pluginApiVersion}_1",
    ":plugins:gist" to "${pluginApiVersion}_1",
    ":plugins:note" to "${pluginApiVersion}_1",
    ":plugins:open-file" to "${pluginApiVersion}_1",
    ":plugins:open-url" to "${pluginApiVersion}_2",
    ":plugins:script" to "${pluginApiVersion}_6",
    ":plugins:separator" to "${pluginApiVersion}_1",
    ":plugins:show-image" to "${pluginApiVersion}_1",
    ":plugins:show-image-svg" to "${pluginApiVersion}_1",
    ":plugins:template" to "${pluginApiVersion}_1",
    ":plugins:text" to "${pluginApiVersion}_3",
    ":plugins:todoist" to "${pluginApiVersion}_1",
    ":plugins:url-parser" to "${pluginApiVersion}_1",
    ":plugins:jira" to "${pluginApiVersion}_11",
    ":plugins:temporary-file" to "${pluginApiVersion}_2",
    ":application-ui-swing" to "0.25-alfa",
)

// Artifact names are kept exactly as in the Maven POMs so that the produced
// JAR names stay compatible with the release scripts.
val moduleArtifactIds = moduleVersions.keys.associateWith { modulePath ->
    when (modulePath) {
        ":application-core" -> "rocket-action-application-core"
        ":application-ui-swing" -> "rocket-action-application-ui-swing"
        ":utils" -> "rocket-action-utils"
        ":ui-utils" -> "rocket-action-ui-utils"
        ":plugins:api" -> "rocket-action-plugin-api"
        else -> "rocket-action-plugin-${modulePath.substringAfterLast(':')}"
    }
}

allprojects {
    group = "ru.ezhov"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://packages.atlassian.com/maven/repository/public")
    }
}

subprojects {
    // ":plugins" only aggregates plugin modules and has no sources of its own.
    if (path == ":plugins") {
        return@subprojects
    }

    version = moduleVersions.getValue(path)
    val artifactId = moduleArtifactIds.getValue(path)

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")

    extensions.configure<BasePluginExtension> {
        archivesName.set(artifactId)
    }

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // In Maven the directly declared kotlin-stdlib 1.8.10 won over transitive
    // versions (nearest-wins). Gradle picks the highest version by default,
    // which would pull kotlin-stdlib 2.1.x via MCP and break the Kotlin 1.8
    // compiler. Pin the Kotlin runtime to the Maven version (kotlin-stdlib-common
    // is pinned too so that modules excluding kotlin-stdlib still resolve the
    // 1.8.10 metadata jar, as Maven does).
    configurations.all {
        resolutionStrategy {
            force(
                "org.jetbrains.kotlin:kotlin-stdlib:1.8.10",
                "org.jetbrains.kotlin:kotlin-stdlib-common:1.8.10",
                // kotlin-logging-jvm 2.1.21 declares the 1.6.0 JDK7/JDK8 stubs;
                // Maven resolves exactly these versions alongside kotlin-stdlib
                // 1.8.10 (the stubs contain no implementation classes).
                "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.0",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0",
                // Spring 6 (Spring Boot 3) requires SLF4J 2.x and Logback 1.4.x.
                "org.slf4j:slf4j-api:2.0.16",
                "ch.qos.logback:logback-classic:1.4.14",
                "ch.qos.logback:logback-core:1.4.14",
            )
        }
    }

    // Plugin POMs declare kotlin-stdlib with "provided" scope, so Maven never
    // packages it into *-fat.jar (the application supplies it at runtime).
    // CompileOnly drops the direct dependency, but transitive dependencies can
    // still bring kotlin-stdlib to the runtime classpath, so exclude it from
    // the fat jar explicitly.
    if (path.startsWith(":plugins:")) {
        configurations.named("runtimeClasspath") {
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        }
    }

    // In Maven the "provided" scope is available on the test classpath.
    // Gradle's compileOnly is not, so make testImplementation inherit it.
    configurations.named("testImplementation") {
        extendsFrom(configurations.named("compileOnly").get())
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // Compile against the Java 17 API so that the build cannot accidentally
        // use APIs from a newer JDK it happens to run on.
        options.release.set(17)
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + "-Xjdk-release=17"
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // Maven resources filtering replaced ${project.version} in properties files.
    // ReplaceTokens is used instead of expand() so that unrelated placeholders
    // such as ${LOGGER_NAME:-ru.ezhov} and "${2 * 2}" are left untouched.
    tasks.withType<ProcessResources>().configureEach {
        filesMatching("**/*.properties") {
            filter(
                ReplaceTokens::class,
                "beginToken" to "\${",
                "endToken" to "}",
                "tokens" to mapOf("project.version" to project.version.toString()),
            )
        }
    }

    val fatJar = tasks.register<Jar>("fatJar") {
        group = "build"
        description = "Builds a JAR containing the module classes and all runtime dependencies."
        archiveFileName.set("$artifactId-fat.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            attributes(
                "Implementation-Title" to artifactId,
                "Implementation-Version" to project.version,
            )
        }

        from(project.extensions.getByType<SourceSetContainer>().getByName("main").output)

        val runtimeClasspath = configurations.named("runtimeClasspath")
        dependsOn(runtimeClasspath)
        from(runtimeClasspath.map { files -> files.map { if (it.isDirectory) it else zipTree(it) } })

        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/INDEX.LIST")
    }

    tasks.named("assemble") {
        dependsOn(fatJar)
    }
}
