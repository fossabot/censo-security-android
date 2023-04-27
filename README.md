#Censo Custody Android

# App Environments and Package Ids
    - Debug: com.censocustody.android.debug
    - PreRelease: com.censocustody.android.prerelease
    - PreProd: com.censocustody.android.preprod
    - Release: com.censocustody.android

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

`
#mac/linux
./gradlew lintDebug

#windows
gradlew.bat lintDebug
`

### Unit Tests

To run unit tests, use the gradle unit test command with the name of the environment.

There is added visual confirmation to see which tests passed or failed on the command line.

`
#mac/linux
./gradlew testDebugUnitTest

#windows
gradlew.bat testDebugUnitTest
`

# Creating A Test Build

2 main ways to create a test build.

Both ways will run lint, unit tests, then create a build for Firebase App Tester distribution.

You then need to go into Firebase App Tester to add build notes and which testers you want to give access to the build.

### Branch Trigger

Pushing to:

`develop` will create a prerelease build.

`staging` will create a preprod build.

`master` will create a release build.

### Local Script

Run the local script `upload_build.sh`

Need to pass one argument: `--token` which is the firebase token needed to upload builds to Firebase App Tester.


# Testing Deep Link

adb shell am start -W -a android.intent.action.VIEW -d "censo://login/[EMAIL_HERE]/[TOKEN_HERE]" com.censocustody.android.[VARIANT_SUFFIX]


# Architecture

INSERT IMAGE FOR ARCHITECTURE HERE

We use a MVVM (Model - View - ViewModel) Architecture


### View Layer

The view layer is implemented using `Jetpack Compose`

Each screen accesses one or more `ViewModels`.

The screens observe a `State` object that is constantly emitted from the `ViewModel(s)`

`Views`also send UI events to the `ViewModel`

The view layer is constantly destroyed and re-built based on different actions. If the phone changes configuration, the app is quickly backgrounded, a language change, or any number of items.
- It is very ephemeral and tracks very little user state (how far a user has scrolled , where the current user is in a text input field, etc)
- All state that needs to be persisted is held in the `ViewModel`

The screen itself is a `composable` and  is `composed` of many different smaller `composables`. A `composable`is a unit of UI and creates a UI tree. `Composables` are implemented with Kotlin functions. Each `composable` will re-draw itself if any of the function parameters change.

Each screen handles certain `Side Effects`
- `Launched Effect` : one off events that the screen wants to respond to. Some examples would be triggering a biometry prompt, navigating away from the screen after an action is completed, etc.
- `Disposable Effect` : lifecycle events that the screen wants to respond to. Inform the ViewModel that the screen has started or is being disposed.


### View Model Layer

The `ViewModel` handles all the logic associated with `State` changes and persists user state beyond configuration changes.

Each `ViewModel`emits a single `State` object which can be observed by several `Views`. The `ViewModel`has no knowledge of who is observing it. The `ViewModel`exposes methods so that `Views`can pass any User or System Events to the `ViewModel.`
- If a user updates their email we will call an `updateEmail`method on the `SignInViewModel` and the `ViewModel`will then update the email in the `State`object and emit a new `State` value.

`ViewModels` can access the `Repository` layer only from `Repository` interfaces. It never accesses the individual Data sources itself.

This is the most tested layer in our codebase. We write unit tests that mock the `repository`layer and then we assert against the state held in the `ViewModel`to ensure it has the expected values.

`ViewModels` should not contain any code from the Android SDK. This way they can be heavily unit tested and those tests will not need an Android device to run.


#### State
`State` objects are simple Kotlin data classes. Every value must have a default value. The `ViewModel`makes copies of the existing state whenever it needs to emit a new state value.

`State` is not exposed to the view layer and can only be set inside the `ViewModel`.

`Resource` types in the `State` classes to represent Asynchronous `State.` `Resource`types have the 4 following types:
- Success
- Error
- Loading
- Uninitialized


### Repository/Data Layer

The `Repository` layer handles all work involved with data sources.

`ViewModels`inject whichever `Repositories` they need and ask them to retrieve data, save data, sign data, verify data, etc.

All `Repositories`are interfaces and then have a single implementation for the main codebase. During unit tests for the `ViewModel` layer, each `Repository`can be mocked and we can test how the `State`updates according to different data.

Each `Repository` extends from the `BaseRepository` to centralize all API error handling.

The current repositories are:
- `UserRepository`
- `KeyRepository`
- `ApprovalsRepository`
- `PushRepository`

The current data sources in the app are:
- `Brooklyn API`
- `Anchor API`
- `SemVer API`
- `Shared Preferences` (simple local storage)
- `Encrypted Storage`
- `Android Keystore`


## Networking

We use the Retrofit Library to handle all of our networking.

We use the OkHttpClient and GSON parsing library plugins with the Retrofit library.

To define an endpoint we simply just add a method to the corresponding API Interface.

`BrooklynAPIService`
- This is our largest API service
- We have an `AuthInterceptor`attached to


##Navigation

All screens are registered in the `MainActivity` under the `CensoNavHost`

The initial screen is the EntranceRoute.

All routes are registered in the `Screen` object.

We can pass `NavigationArguments` to the screen and then when the screen starts, it will forward them directly to the `ViewModel.`

When the app needs to navigate to the next screen it simply accesses the `NavigationController`and provides it with the route.

We have one deeplink route: `TokenLoginScreen`: censo://login/\{userEmail\}/\{token\}”
- We grab the email and token from the route.
  - We only match a route that has both the email and token arguments.
- We send the user to the `TokenSignInScreen`with the email and token arguments.

## Storage

## Cryptography

### `Encryption Manager`

### `Cryptography Manager`

## Unit Test

## Push Notifications

## 3rd Party Libraries

## Main Flows

### Auth/Sign In

### Entrance Flow

### Approval Signing Flow

1. Create the `ApprovalDispositionRequest`
2. Call `convertToApiBody`on the `ApprovalDispositionRequest`
  1. Call `retrieveSignableData`
  2. Create `signatures` for the signable data by calling the `signApprovalDisposition` on the`EncryptionManager`
  3. Create any `recoveryShards`
  4. Crete any `reShareShards`

`ApprovalDispositionRequest`
- Call `retrieveSignableData` for each Approval Type
