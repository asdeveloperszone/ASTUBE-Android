#!/bin/sh
# Gradle wrapper script
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME=`pwd -P`

MAX_FD="maximum"

warn () { echo "$*"; }
die () { echo; echo "$*"; echo; exit 1; }

OS_NAME=`uname -s`
case $OS_NAME in
  Darwin*) os_type="mac";;
  CYGWIN*) os_type="cygwin";;
  MINGW*)  os_type="mingw";;
  MSYS*)   os_type="msys";;
  *)       os_type="unix";;
esac

JAVA_EXE=java
if [ -n "$JAVA_HOME" ]; then
    JAVA_EXE="$JAVA_HOME/bin/java"
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec "$JAVA_EXE" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
