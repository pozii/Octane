@echo off
rem Octane gradle wrapper launcher.
rem Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
java -cp "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
