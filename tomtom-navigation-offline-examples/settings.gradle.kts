/*
 * © 2023 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

rootProject.name = "tomtom-sdk-examples"

fun RepositoryHandler.tomtomArtifactory() {
    maven("https://repositories.tomtom.com/artifactory/maven") {
        credentials {
            username = extra["repositoriesTomtomComUsername"].toString()
            password = extra["repositoriesTomtomComPassword"].toString()
        }
        content { includeGroupByRegex("com\\.tomtom\\..+") }
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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

