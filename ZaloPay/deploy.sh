#!/bin/bash

BUILD_TYPE=$1

if [ "$BUILD_TYPE" = "staging" ]
then
	./gradlew assembleStagingRelease 
	./gradlew crashlyticsUploadDistributionStagingRelease
elif [ "$BUILD_TYPE" = "production" ]
then
	./gradlew assembleProductionRelease 
	./gradlew crashlyticsUploadDistributionProductionRelease
else 
	./gradlew assembleSandboxRelease 
	./gradlew crashlyticsUploadDistributionSandboxRelease
fi

exit

