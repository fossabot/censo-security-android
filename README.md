# App Environments and Package Ids
    - Debug: com.strikeprotocols.mobile.debug
    - PreRelease: com.strikeprotocols.mobile.prerelease
    - DemoOne: com.strikeprotocols.mobile.demo
    - DemoTwo: com.strikeprotocols.mobile.demo2
    - Release: com.strikeprotocols.mobile

# Build Setup

### Point to Android SDK location

Create `local.properties` file in root and add the location
of the Android SDK on your local machine.

Typically will be located here: `sdk.dir=/Users/[USERNAME]/Library/Android/sdk`

* Android Studio/IntelliJ will do this by default for you just by loading
  the project and either running the app or doing a gradle sync.

### All Environments Setup

1. Move `config.properties` to the root of the project
2. Add `google-services.json` to `app/google-services.json`
3. Add `keystore.jks` to `app/keystore.jks`

### Debug Environment Only Setup

1. Create project on Firebase and add app for each environment you need (must match app ids above)
2. Add `google-services.json` from Firebase project to `app/google-services.json`

# Lint & Tests

### Linting

To run lint, use the gradle lint command with the name of the environment.

The command will fail if there are any lint warnings.

```
#mac/linux
./gradlew lintDebug

#windows
gradlew.bat lintDebug
```

### Unit Tests

To run unit tests, use the gradle unit test command with the name of the environment.

There is added visual confirmation to see which tests passed or failed on the command line.

```
#mac/linux
./gradlew testDebugUnitTest

#windows
gradlew.bat testDebugUnitTest
```