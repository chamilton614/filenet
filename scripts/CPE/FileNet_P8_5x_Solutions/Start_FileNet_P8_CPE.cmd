@echo off
REM Start FileNet P8 CPE
cls
title Start FileNet CPE

REM WebSphere Profile Root Path
if exist "C:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=C:\IBM\WebSphere\AppServer\profiles\AppSrv01
if exist "D:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=D:\IBM\WebSphere\AppServer\profiles\AppSrv01

REM Start CPE Server
echo ===================
echo Start CPE
echo ===================
REM call %WebSphereProfileRoot%\bin\startServer.bat server1
call %WebSphereProfileRoot%\bin\startServer.bat server1 -username cpeadmin-dv -password P@ssw0rd
echo.

:End
REM pause
