#!/bin/bash

BUILD_TYPE=$1

if [ "$BUILD_TYPE" = "staging" ]
then
	./gradlew clean assembleStagingRelease
	./gradlew crashlyticsUploadDistributionStagingRelease
elif [ "$BUILD_TYPE" = "production" ]
then
	./gradlew clean assembleProductionRelease
	./gradlew crashlyticsUploadDistributionProductionRelease
else 
	./gradlew clean assembleSandboxRelease
	./gradlew crashlyticsUploadDistributionSandboxRelease
fi

exit

