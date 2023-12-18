## Offline navigation Use Case for Android ##

This project is a sample app to show how to run offline modules using the TomTom Navigation SDK on Android.

- [Offline functionality for the TomTom Navigation SDK for Android](https://developer.tomtom.com/android/navigation/documentation/guides/offline/quickstart)

> **Note:** It's not a complete app that includes all TomTom Navigation SDK functionalities and implementations.

<div align="center">
  <img align="center" src="assets/nav-sdk-offline.png" width="400"/>
</div> <br>

This is a reference implementation for the Offline Navigation Use Case tutorial for TomTom Navigation SDK for
Android [https://developer.tomtom.com/android/navigation/documentation/tutorials/offline-navigation-use-case](https://developer.tomtom.com/android/navigation/documentation/tutorials/offline-navigation-use-case).

It does not include integration of the TPEG traffic module described in the tutorial.

An empty NDS store is included and this is populated using the device location, loading relevant map regions around the
current location. For more information on offline map updates see the
guide [https://developer.tomtom.com/android/navigation/documentation/guides/offline/quickstart](https://developer.tomtom.com/android/navigation/documentation/guides/offline/quickstart)

## Features

- Auto-download offline map for region around the user location
- Long press on the map to start a navigation
- Tap on the search result to start a navigation
- Navigation view
    - Next instruction view
    - Route line progress view
    - Estimated time of arrival (ETA) view
    - Lane guidance view
    - Combined instruction view
    - Route updated view

## Setup

Because the repository for TomTom Navigation SDK is private, you will need
to [contact us](https://developer.tomtom.com/tomtom-sdk-for-android/request-access) to get access. Once you have
obtained access,

- Go to [repositories.tomtom.com](https://repositories.tomtom.com/) and log in with your account.
- Expand the user menu in the top-right corner, and select "Edit profile" → "Generate an Identity Token".
- Copy your token and paste into global gradle.properties as mentioned in the next step.

### Add gradle.properties file

Add the entries below to the global `$HOME/.gradle/gradle.properties` file.

```bash
# required to access artifactory
repositoriesTomTomComUsername=###
repositoriesTomTomComPassword=###

# required in order to use TomTom's APIs
tomtomApiKey=###
ndsMapLicense=###
```

### Running the app

- The first time you launch the app you will be prompted to allow the app access to your location. Choose "While using
  the app".
- Initially the globe will be blank as the app is built with an empty NDS store. The app will need to be connected to
  the internet initially to download the map data for offline use.
- After a few seconds the current location marker will appear. Click the current location button to center on your
  location. The map data for the regions around the location will be downloaded. This can take around 30 seconds. After
  that time panning or zooming the map will trigger rendering with the downloaded map data.

## Debugging

The app is configured to default download the region around the user location. After running the project, the map
downloading process will start automatically. It may take time to download the map. You can follow the process in
the `Network Inspector` in `App Inspection` tool within Android Studio.

## Preview

You can see the downloaded map when the downloading process finished as below.

| Zoomed Out                                                             | Region Downloaded                                                                          | 
|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| <img src="assets/offline-zoomed-out.png" width="250" alt="Zoomed Out"> | <img src="assets/offline-amsterdam-downloaded.png" width="250" alt="Amsterdam Downloaded"> |

## Relevant map regions along the route

You can enable the relevant maps region configuration as below.

```
val relevantRegionUpdateConfig: NdsStoreUpdateConfig = defaultUpdateConfig.copy(
    automaticUpdatesConfiguration = defaultUpdateConfig.automaticUpdatesConfiguration.copy(
        relevantRegionsEnabled = true,
        relevantRegionsRadius = Distance.kilometers(10.0),
        relevantRegionsUpdateInterval = 60.minutes
    )
)
```

When you set a route destination without a downloaded region, the region will be downloaded automatically and rendered
in the map during the navigation continues.

## Subdirectories
- [Link to Use Case README](usecase/README.md)