#!/bin/sh
#
#    Licensed to the Apache Software Foundation (ASF) under one or more
#    contributor license agreements.  See the NOTICE file distributed with
#    this work for additional information regarding copyright ownership.
#    The ASF licenses this file to You under the Apache License, Version 2.0
#    (the "License"); you may not use this file except in compliance with
#    the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#
# $Id: servicemix 979 2005-11-30 22:50:55Z bsnyder $
#

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

#
# Check/Set up some easily accessible MIN/MAX params for JVM mem usage
#

if [ "x$JAVA_MIN_MEM" = "x" ]; then
    JAVA_MIN_MEM=128M
    export JAVA_MIN_MEM
fi

detectOS() {
    # OS specific support (must be 'true' or 'false').
    cygwin=false;
    darwin=false;
    aix=false;
    os400=false;
    case "`uname`" in
        CYGWIN*)
            cygwin=true
            ;;
        Darwin*)
            darwin=true
            ;;
        AIX*)
            aix=true
            ;;
        OS400*)
            os400=true
            ;;
    esac
    # For AIX, set an environment variable
    if $aix; then
         export LDR_CNTRL=MAXDATA=0xB0000000@DSA
         export IBM_JAVA_HEAPDUMP_TEXT=true
         echo $LDR_CNTRL                           
    fi
}

unlimitFD() {
    # Use the maximum available, or set MAX_FD != -1 to use that
    if [ "x$MAX_FD" = "x" ]; then
        MAX_FD="maximum"
    fi
    
    # Increase the maximum file descriptors if we can
    if [ "$os400" = "false" ] && [ "$cygwin" = "false" ]; then
        MAX_FD_LIMIT=`ulimit -H -n`
        if [ $? -eq 0 ]; then
            if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ]; then
                # use the system max
                MAX_FD="$MAX_FD_LIMIT"
            fi
            
            ulimit -n $MAX_FD
            # echo "ulimit -n" `ulimit -n`
            if [ $? -ne 0 ]; then
                warn "Could not set maximum file descriptor limit: $MAX_FD"
            fi
        else
            warn "Could not query system maximum file descriptor limit: $MAX_FD_LIMIT"
        fi
    fi
}

setupNativePath() {
    # Support for loading native libraries
    LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:$SERVICEMIX_HOME/lib"
    
    # For Cygwin, set PATH from LD_LIBRARY_PATH
    if $cygwin; then
        LD_LIBRARY_PATH=`cygpath --path --windows "$LD_LIBRARY_PATH"`
        PATH="$PATH;$LD_LIBRARY_PATH"
        export PATH
    fi
    export LD_LIBRARY_PATH
}

locateJava() {
    # Setup the Java Virtual Machine
    if $cygwin ; then
        [ -n "$JAVA" ] && JAVA=`cygpath --unix "$JAVA"`
        [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    fi
    
    if [ "x$JAVA" = "x" ]; then
        if [ "x$JAVA_HOME" != "x" ]; then
            if [ ! -d "$JAVA_HOME" ]; then
                die "JAVA_HOME is not valid: $JAVA_HOME"
            fi
            JAVA="$JAVA_HOME/bin/java"
        else
            warn "JAVA_HOME not set; results may vary"
            JAVA="java"
        fi
    fi
}

detectJVM() {
   echo "`$JAVA -version`"
   # This service should call `java -version`, 
   # read stdout, and look for hints
   if $JAVA -version 2>&1 | grep "^IBM" ; then
       JVM_VENDOR="IBM"
   # on OS/400, java -version does not contain IBM explicitly
   elif $os400; then
       JVM_VENDOR="IBM"
   else
       JVM_VENDOR="SUN"
   fi
   # echo "JVM vendor is $JVM_VENDOR"
}

locateHome() {
    if [ "x$SERVICEMIX_HOME" != "x" ]; then
        warn "Ignoring predefined value for SERVICEMIX_HOME"
    fi
    
    SERVICEMIX_HOME=`cd $DIRNAME/..; pwd`
    if [ ! -d "$SERVICEMIX_HOME" ]; then
        die "SERVICEMIX_HOME is not valid: $SERVICEMIX_HOME"
    fi
}

init() {
    # Determine if there is special OS handling we must perform
    detectOS
    
    # Unlimit the number of file descriptors if possible
    unlimitFD
    
    # Locate the ServiceMix home directory
    locateHome
    
    # Locate the Java VM to execute
    locateJava
    
    # Determine the JVM vendor
    detectJVM
}

run() {
    JAR=$SERVICEMIX_HOME/bin/bootstrapper.jar
    CLASSPATH=$SERVICEMIX_HOME/bin/bootstrapper.jar:$SERVICEMIX_HOME/bin/daemon.jar:$SERVICEMIX_HOME/bin/logger.jar:$SERVICEMIX_HOME/bin/servicemix.jar
    if $cygwin; then
        SERVICEMIX_HOME=`cygpath --path --windows "$SERVICEMIX_HOME"`
        CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
        CLASSWORLDS_CONF=`cygpath --path --windows "$CLASSWORLDS_CONF"`
        CYGHOME=`cygpath --windows "$HOME"`
        JAR=`cygpath --windows "$JAR"`
    fi
    cd $SERVICEMIX_HOME/conf
    exec $JAVA -Dfelix.home=$SERVICEMIX_HOME -jar $JAR $SERVICEMIX_HOME start 
}

main() {
    init
    run $@
}

main $@
