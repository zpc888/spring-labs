// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("java-library")
}

allprojects {
    group = "ca.prot"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}
