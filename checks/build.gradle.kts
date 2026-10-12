plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_check_api:2.50.0")
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
}

// Writing a BugChecker means importing com.sun.tools.javac.* directly. The
// net.ltgt.errorprone plugin patches the *consuming* module's compileJava task
// for this; this module compiles the checker itself, so it needs the same
// flags on its own compileJava.
val javacInternalsForErrorProne = listOf(
    "jdk.compiler/com.sun.tools.javac.api",
    "jdk.compiler/com.sun.tools.javac.code",
    "jdk.compiler/com.sun.tools.javac.comp",
    "jdk.compiler/com.sun.tools.javac.file",
    "jdk.compiler/com.sun.tools.javac.main",
    "jdk.compiler/com.sun.tools.javac.model",
    "jdk.compiler/com.sun.tools.javac.parser",
    "jdk.compiler/com.sun.tools.javac.processing",
    "jdk.compiler/com.sun.tools.javac.tree",
    "jdk.compiler/com.sun.tools.javac.util",
)

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(javacInternalsForErrorProne.flatMap { listOf("--add-exports", "$it=ALL-UNNAMED") })
}
