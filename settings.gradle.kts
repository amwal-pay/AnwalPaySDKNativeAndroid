import java.io.File
import java.util.Properties

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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://storage.googleapis.com/download.flutter.io")
        }
    }
}

fun checkPropertyInLocalProperties(propertyName: String, defaultValue: Boolean): Boolean {
    val properties = Properties()
    val localProperties = File(settingsDir, "local.properties")

    if (localProperties.exists()) {
        properties.load(localProperties.inputStream())
        return properties.getProperty(propertyName, defaultValue.toString()).toBoolean()
    }
    return defaultValue
}




rootProject.name = "AnwalPaySDKExample"
include(":app")



