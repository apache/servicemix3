@echo off
rem
rem
rem    Licensed to the Apache Software Foundation (ASF) under one or more
rem    contributor license agreements.  See the NOTICE file distributed with
rem    this work for additional information regarding copyright ownership.
rem    The ASF licenses this file to You under the Apache License, Version 2.0
rem    (the "License"); you may not use this file except in compliance with
rem    the License.  You may obtain a copy of the License at
rem
rem       http://www.apache.org/licenses/LICENSE-2.0
rem
rem    Unless required by applicable law or agreed to in writing, software
rem    distributed under the License is distributed on an "AS IS" BASIS,
rem    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem    See the License for the specific language governing permissions and
rem    limitations under the License.
rem
rem 
rem $Id: servicemix.bat 979 2005-11-30 22:50:55Z bsnyder $
rem 

if "%SERVICEMIX_VERSION%" == "" (
    set SERVICEMIX_VERSION=@{servicemix-version}
)

if "%DEF_GROUP_ID%" == "" (
    set DEF_GROUP_ID=com.mycompany
)

if ""%1"" == ""sl"" goto doSL
if ""%1"" == ""se"" goto doSE
if ""%1"" == ""bc"" goto doBC
if ""%1"" == ""sa"" goto doSA
if ""%1"" == ""su"" goto doSU

set EMPTY=
echo Usage: smx-arch.bat ( commands ... )
echo commands:
echo   sl                Creates a new Shared Library
echo   se                Creates a new Service Engine
echo   bc                Creates a new Binding Component
echo   sa                Creates a new Service Assembly
echo   su                Creates a generic Service Unit
echo   su [type]         Creates a SU of the specified type
echo SU types:         
echo                     http-consumer, http-provider,
echo                     jms-consumer, jms-provider,
echo                     file-poller, file-sender,
echo                     ftp-poller, ftp-sender,
echo                     jsr181-annotated, jsr181-wsdl-first, mail, 
echo                     saxon-xquery, saxon-xslt, osworkflow,
echo                     eip, lwcontainer, bean, ode, camel, scripting,
echo                     cxf-se, cxf-se-wsdl-first, cxf-bc
echo Optional arguments:
echo   -DgroupId=xxxx
echo   -DartifactId=xxxx
goto end

:doSL
  set ARCHETYPE=servicemix-shared-library
  set DEF_ARTIFACT_ID=my-sl
  shift
goto run

:doSE
  set ARCHETYPE=servicemix-service-engine
  set DEF_ARTIFACT_ID=my-se
  shift
goto run

:doBC
  set ARCHETYPE=servicemix-binding-component
  set DEF_ARTIFACT_ID=my-bc
  shift
goto run

:doSA
  set ARCHETYPE=servicemix-service-assembly
  set DEF_ARTIFACT_ID=my-sa
  shift
goto run

:doSU
if not ""%2"" == """" goto doTypedSU
  set ARCHETYPE=servicemix-service-unit
  set DEF_ARTIFACT_ID=my-su
  shift
goto run

:doTypedSU
  set ARCHETYPE=servicemix-%2-service-unit
  set DEF_ARTIFACT_ID=my-%2-su
  shift
  shift
goto run

:run
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs
mvn archetype:create -DremoteRepositories=@{releases-repo-url} -DarchetypeGroupId=org.apache.servicemix.tooling -DarchetypeArtifactId=%ARCHETYPE% -DarchetypeVersion=%SERVICEMIX_VERSION% -DgroupId=%DEF_GROUP_ID% -DartifactId=%DEF_ARTIFACT_ID% %CMD_LINE_ARGS%
goto end

:end
