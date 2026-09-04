#!/bin/sh
# Octane gradle wrapper launcher.
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -cp "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
