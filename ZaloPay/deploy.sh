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
elif [ "$BUILD_TYPE" = "sandbox" ]
then
	./gradlew clean assembleSandboxRelease
	./gradlew crashlyticsUploadDistributionSandboxRelease
else 
	echo "Environment invalid -> * Try: [sandbox/staging/production]"
fi

exit

