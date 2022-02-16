# Strike Mobile

## Linting

To run lint, use the gradle lint command with the name of the environment.

The command will fail if there are any lint warnings.

```
#mac/linux
./gradlew lintDebug

#windows
gradlew.bat lintDebug
```

## Unit Tests

To run unit tests, use the gradle unit test command with the name of the environment.

There is added visual confirmation to see which tests passed or failed on the command line.

```
#mac/linux
./gradlew testDebugUnitTest

#windows
gradlew.bat testDebugUnitTest
```