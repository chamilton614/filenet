@echo off
REM Stop FileNet P8 CPE
cls
title Stop FileNet CPE

REM WebSphere Profile Root Path
if exist "C:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=C:\IBM\WebSphere\AppServer\profiles\AppSrv01
if exist "D:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=D:\IBM\WebSphere\AppServer\profiles\AppSrv01

REM Stop CPE Server
echo ===================
echo Stop CPE
echo ===================
REM call %WebSphereProfileRoot%\bin\stopServer.bat server1
call %WebSphereProfileRoot%\bin\stopServer.bat server1 -username cpeadmin-dv -password P@ssw0rd
echo.

:End
REM pause
