// Workaround for AGP AndroidLocationsException when both ANDROID_PREFS_ROOT and ANDROID_USER_HOME are set
try {
    val pe = Class.forName("java.lang.ProcessEnvironment")
    for (fieldName in listOf("theEnvironment", "theUnmodifiableEnvironment", "theCaseInsensitiveEnvironment")) {
        try {
            val field = pe.getDeclaredField(fieldName)
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val map = field[null] as? MutableMap<String, String>
            map?.remove("ANDROID_PREFS_ROOT")
        } catch (_: Throwable) {}
    }
} catch (_: Throwable) {}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Zarur"
include(":app")
