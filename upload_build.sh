#! /bin/bash

set -e

while [ $# -gt 0 ]; do
    if [[ $1 == "--"* ]]; then
        v="${1/--/}"
        declare "$v"="$2"
        shift
    fi
    shift
done

if [[ -z $token ]]; then
  printf "Missing firebase token. Make sure to pass token as: --token argument.\n"
  exit 1
fi

environment="Debug"

prerelease="PreRelease"
release="Release"
preprod="PreProd"

PS3='Please choose environment: '
options=("$prerelease" "$preprod" "$release")
select opt in "${options[@]}"
do
    case $opt in
        "$prerelease")
            environment=$prerelease
            echo "Creating PreRelease build..."
            break
            ;;
        "$preprod")
            echo "Creating PreProd build..."
            environment=$preprod
            break
            ;;
        "$release")
            echo "Creating Release build..."
            environment=$release
            break
            ;;
        *) echo "invalid option $REPLY";;
    esac
done

./gradlew --stop
echo Running lint"${environment}"
./gradlew lint"${environment}"
echo Running test"${environment}"UnitTest
./gradlew test"${environment}"UnitTest

export FIREBASE_TOKEN="$token"
echo "$FIREBASE_TOKEN"

echo Running assemble"${environment}" appDistributionUpload"${environment}"
./gradlew assemble"${environment}" appDistributionUpload"${environment}"