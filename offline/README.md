## Offline navigation Use Case for Android ##

:warning: Uses SDK version **0.34.0**. Changes may be required for later SDK versions.

This is a reference implementation for the Offline Navigation Use Case tutorial for TomTom Navigation SDK for Android [https://developer.tomtom.com/android/navigation/documentation/tutorials/offline-navigation-use-case](https://developer.tomtom.com/android/navigation/documentation/tutorials/offline-navigation-use-case).

It does not include integration of the TPEG traffic module described in the tutorial.

An empty NDS store is included and this is populated using the device location, loading relevant map regions around the current location. For more information on offline map updates see the guide [https://developer.tomtom.com/android/navigation/documentation/guides/offline-maps](https://developer.tomtom.com/android/navigation/documentation/guides/offline-maps)

### Building the app ###

Before building the app you will need access to the Navigation SDK, an API key enabled for navigation and map updates, and an NDS map license key. You can request access here [https://developer.tomtom.com/tomtom-sdk-for-android/request-access](https://developer.tomtom.com/tomtom-sdk-for-android/request-access)

### Add gradle.properties file
Add the entries below to the global `~/.gradle/gradle.properties` file.

```bash
# required for accessing to artifactory
repositoriesTomTomComUsername=###
repositoriesTomTomComPassword=###

# required in order to use TomTom's APIs
tomtomApiKey=###
ndsMapLicense=###
```

### Running the app ###
- The first time you launch the app you will be prompted to allow the app access to your location. Choose "While using the app".
- Initially the globe will be blank as the app is built with an empty NDS store. The app will need to be connected to the internet initially to download the map data for offline use.
- After a few seconds the current location marker will appear. Click the current location button to center on your location. The map data for the regions around the location will be downloaded, this can take around 30 seconds. After that time panning or zooming the map will trigger rendering with the downloaded map data.