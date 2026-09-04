@echo off
rem Octane gradle wrapper launcher.
java -cp "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
