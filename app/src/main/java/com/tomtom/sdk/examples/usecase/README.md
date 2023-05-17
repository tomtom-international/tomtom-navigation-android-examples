# Simple navigation app using TomTom SDK

*Note: Navigation SDK for Android is only available upon request. [Contact us](https://developer.tomtom.com/tomtom-sdk-for-android/request-access "Contact us") to get started.*

## Online navigation example

This example shows how to build a simple navigation application using the TomTom Navigation SDK for Android.
The application displays a map and shows the user’s location. After the user selects a destination with a long click, the app plans a route and draws it on the map. Navigation is started once the user taps on the route.

For further explanations on the example provided can be found in this [tutorial](https://developer.tomtom.com/android/navigation/documentation/tutorials/navigation-use-case).

## Offline navigation example

*Note: The use of offline navigation features requires a separate eval agreement. Contact your TomTom account manager to arrange access.*

The offline map example shows how to use offline maps using the TomTom Navigation SDK for Android.

To run the offline navigation example the NDS map license provided to you by TomTom needs to be added to the global `~/.gradle/gradle.properties` file.

```bash

ndsMapLicense = NDS-MAP-LICENSE
```