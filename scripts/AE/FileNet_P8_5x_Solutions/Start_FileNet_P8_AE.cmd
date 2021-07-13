@echo off
REM Start FileNet P8 AE
cls
title Start FileNet AE

REM WebSphere Profile Root Path
if exist "C:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=C:\IBM\WebSphere\AppServer\profiles\AppSrv01
REM if exist "D:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=D:\IBM\WebSphere\AppServer\profiles\AppSrv01

REM Start AE Server
echo ===================
echo Start AE
echo ===================
REM call %WebSphereProfileRoot%\bin\startServer.bat server1
call %WebSphereProfileRoot%\bin\startServer.bat server1 -username cpeadmin-dv -password P@ssw0rd
echo.

REM Start VWServices AE
echo ========================
echo Start VWServices for AE
echo ========================
net start VWServicesAE
echo.

REM Start VWServices WP XT
echo ========================
echo Start VWServices for XT
echo ========================
net start VWServicesWPXT
echo.

:End
REM pause