#!/bin/bash

./gradlew installDebug
for var in "$@"
do
	adb -s $var shell am start -n io.fluentic.ubicdn/.ui.splash.SplashActivity
done
