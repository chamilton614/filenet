@echo off
REM Update WebSphere to JDK 1.7
cls
title Update WebSphere to JDK 1.7

REM WebSphere Profile Root Path
if exist "C:\IBM\WebSphere\AppServer" set WebSphereHomeRoot=C:\IBM\WebSphere\AppServer
if exist "D:\IBM\WebSphere\AppServer" set WebSphereHomeRoot=D:\IBM\WebSphere\AppServer

REM List Available JDK
echo ===================
echo List Available JDK
echo ===================
call %WebSphereHomeRoot%\bin\managesdk -listAvailable -verbose
echo.

REM Update the Command Default JDK
echo ===============================
echo Update the Command Default JDK
echo ===============================
call %WebSphereHomeRoot%\bin\managesdk -setCommandDefault -sdkname 1.7_64
echo.

REM Update the New Profile Default JDK
echo ===================================
echo Update the New Profile Default JDK
echo ===================================
call %WebSphereHomeRoot%\bin\managesdk -setNewProfileDefault -sdkname 1.7_64
echo.

REM Update the Existing Profiles Default JDK
echo =========================================
echo Update the Existing Profiles Default JDK
echo =========================================
call %WebSphereHomeRoot%\bin\managesdk -enableProfileAll -sdkname 1.7_64 -enableServers
echo.

:End
REM pause