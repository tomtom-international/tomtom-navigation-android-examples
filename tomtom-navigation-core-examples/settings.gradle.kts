/*
 * Copyright © 2023. Change this to your own official copyright statement.
 */

rootProject.name = "tomtom-sdk-examples"

fun RepositoryHandler.tomtomArtifactory() {
    maven("https://repositories.tomtom.com/artifactory/maven") {
        content { includeGroupByRegex("com\\.tomtom\\..+") }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        tomtomArtifactory()
        google()
        mavenCentral()
    }
}

include(":usecase")

