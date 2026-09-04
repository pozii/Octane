#!/bin/sh
# Octane gradle wrapper launcher.
# Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -cp "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
