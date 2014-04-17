#!/bin/bash

#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.

#
# Discovers path to Java executable and checks it's version.
# The function exports JAVA variable with path to Java executable.
#

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

checkJava() {
    # Check JAVA_HOME.
    if [ "$JAVA_HOME" = "" ]; then
        JAVA=`which java`
        RETCODE=$?

        if [ $RETCODE -ne 0 ]; then
            echo $0", ERROR:"
            echo "JAVA_HOME environment variable is not found."
            echo "Please point JAVA_HOME variable to location of JDK 1.7 or JDK 1.8."
            echo "You can also download latest JDK at http://java.com/download"

            exit 1
        fi

        JAVA_HOME=
    else
        JAVA=${JAVA_HOME}/bin/java
    fi

    #
    # Check JDK.
    #
    if [ ! -e "$JAVA" ]; then
        echo $0", ERROR:"
        echo "JAVA is not found in JAVA_HOME=$JAVA_HOME."
        echo "Please point JAVA_HOME variable to installation of JDK 1.7 or JDK 1.8."
        echo "You can also download latest JDK at http://java.com/download"

        exit 1
    fi

    JAVA_VER=`"$JAVA" -version 2>&1 | egrep "1\.[78]\."`

    if [ "$JAVA_VER" == "" ]; then
        echo $0", ERROR:"
        echo "The version of JAVA installed in JAVA_HOME=$JAVA_HOME is incorrect."
        echo "Please point JAVA_HOME variable to installation of JDK 1.7 or JDK 1.8."
        echo "You can also download latest JDK at http://java.com/download"

        exit 1
    fi
}

#
# Discover path to Java executable and check it's version.
#
checkJava

ARGS=$*

CP="${SCRIPT_DIR}/../libs/*:${SCRIPT_DIR}/../yardstick-1.0.0.jar"

#
# JVM options. See http://java.sun.com/javase/technologies/hotspot/vmoptions.jsp for more details.
#
# ADD YOUR/CHANGE ADDITIONAL OPTIONS HERE
#
if [ -z "$JVM_OPTS" ] ; then
    JVM_OPTS="-Xms2g -Xmx2g -server -Djava.net.preferIPv4Stack=true"
fi

#
# Assertions are disabled by default.
# If you want to enable them - set 'ENABLE_ASSERTIONS' flag to '1'.
#
ENABLE_ASSERTIONS="0"

#
# Set '-ea' options if assertions are enabled.
#
if [ "${ENABLE_ASSERTIONS}" = "1" ]; then
    JVM_OPTS="${JVM_OPTS} -ea"
fi

MAIN_CLASS=org.yardstick.BenchmarkStartUp

"$JAVA" ${JVM_OPTS} -cp ${CP} ${MAIN_CLASS} ${ARGS}
