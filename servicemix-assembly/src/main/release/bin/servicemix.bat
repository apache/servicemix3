@echo off
rem 
rem $Id: servicemix.bat 979 2005-11-30 22:50:55Z bsnyder $
rem 

if not "%ECHO%" == "" echo %ECHO%

setlocal
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%
set ARGS=%*

title ServiceMix

goto BEGIN

:warn
    echo %PROGNAME%: %*
goto :EOF

:BEGIN

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

if not "%SERVICEMIX_HOME%" == "" (
    call :warn Ignoring predefined value for SERVICEMIX_HOME
)
set SERVICEMIX_HOME=%DIRNAME%..
if not exist "%SERVICEMIX_HOME%" (
    call :warn SERVICEMIX_HOME is not valid: %SERVICEMIX_HOME%
    goto END
)

set LOCAL_CLASSPATH=%CLASSPATH%
set DEFAULT_JAVA_OPTS=-server -Xmx512M -Dderby.system.home="%SERVICEMIX_HOME%\var" -Dderby.storage.fileSyncTransactionLog=true 
set CLASSPATH=%LOCAL_CLASSPATH%;%SERVICEMIX_HOME%\conf
set DEFAULT_JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

if "%LOCAL_CLASSPATH%" == "" goto :SERVICEMIX_CLASSPATH_EMPTY
    set CLASSPATH=%LOCAL_CLASSPATH%;%SERVICEMIX_HOME%\conf
    goto :SERVICEMIX_CLASSPATH_END
:SERVICEMIX_CLASSPATH_EMPTY
    set CLASSPATH=%SERVICEMIX_HOME%\conf
:SERVICEMIX_CLASSPATH_END

rem Setup Servicemix Home
if exist "%SERVICEMIX_HOME%\conf\servicemix-rc.cmd" call %SERVICEMIX_HOME%\conf\servicemix-rc.cmd
if exist "%HOME%\servicemix-rc.cmd" call %HOME%\servicemix-rc.cmd

rem Support for loading native libraries
set PATH=%PATH%;%SERVICEMIX_HOME%\lib

rem Setup the Java Virtual Machine
if not "%JAVA%" == "" goto :Check_JAVA_END
    set JAVA=java
    if "%JAVA_HOME%" == "" call :warn JAVA_HOME not set; results may vary
    if not "%JAVA_HOME%" == "" set JAVA=%JAVA_HOME%\bin\java
    if not exist "%JAVA_HOME%" (
        call :warn JAVA_HOME is not valid: %JAVA_HOME%
        goto END
    )
:Check_JAVA_END

if "%JAVA_OPTS%" == "" set JAVA_OPTS=%DEFAULT_JAVA_OPTS%

if "%SERVICEMIX_DEBUG%" == "" goto :SERVICEMIX_DEBUG_END
    rem Use the defaults if JAVA_DEBUG_OPTS was not set
    if "%JAVA_DEBUG_OPTS%" == "" set JAVA_DEBUG_OPTS=%DEFAULT_JAVA_DEBUG_OPTS%
    
    set "JAVA_OPTS=%JAVA_DEBUG_OPTS% %JAVA_OPTS%"
    call :warn Enabling Java debug options: %JAVA_DEBUG_OPTS%
:SERVICEMIX_DEBUG_END

if "%SERVICEMIX_PROFILER%" == "" goto :SERVICEMIX_PROFILER_END
    set SERVICEMIX_PROFILER_SCRIPT=%SERVICEMIX_HOME%\conf\profiler\%SERVICEMIX_PROFILER%.cmd
    
    if exist "%SERVICEMIX_PROFILER_SCRIPT%" goto :SERVICEMIX_PROFILER_END
    call :warn Missing configuration for profiler '%SERVICEMIX_PROFILER%': %SERVICEMIX_PROFILER_SCRIPT%
    goto END
:SERVICEMIX_PROFILER_END

rem Setup the classpath
set CLASSPATH=%CLASSPATH%;%SERVICEMIX_HOME%\lib\classworlds-1.0.1.jar

rem Setup boot options
set CLASSWORLDS_CONF=%SERVICEMIX_HOME%\conf\servicemix.conf
set BOOT_OPTS=%BOOT_OPTS% -Dclassworlds.conf="%CLASSWORLDS_CONF%"
set BOOT_OPTS=%BOOT_OPTS% -Dservicemix.home="%SERVICEMIX_HOME%"
set BOOT_OPTS=%BOOT_OPTS% -Djava.endorsed.dirs="%SERVICEMIX_HOME%\lib\endorsed"

rem Execute the JVM or the load the profiler
if "%SERVICEMIX_PROFILER%" == "" goto :EXECUTE
    rem Execute the profiler if it has been configured
    call :warn Loading profiler script: %SERVICEMIX_PROFILER_SCRIPT%
    call %SERVICEMIX_PROFILER_SCRIPT%

:EXECUTE
    rem Execute the Java Virtual Machine
    "%JAVA%" %JAVA_OPTS% -classpath "%CLASSPATH%" %BOOT_OPTS% org.codehaus.classworlds.Launcher org.apache.servicemix.Main %ARGS%

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END

endlocal

if not "%PAUSE%" == "" pause

:END_NO_PAUSE

