@echo off
REM Stop FileNet P8 AE
cls
title Stop FileNet AE

REM WebSphere Profile Root Path
if exist "C:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=C:\IBM\WebSphere\AppServer\profiles\AppSrv01
REM if exist "D:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=D:\IBM\WebSphere\AppServer\profiles\AppSrv01

REM Stop VWServices AE
echo ========================
echo Stop VWServices for AE
echo ========================
net stop VWServicesAE
echo.

REM Stop VWServices WP XT
echo ========================
echo Stop VWServices for XT
echo ========================
net stop VWServicesWPXT
echo.

REM Stop AE Server
echo ===================
echo Stop AE
echo ===================
REM call %WebSphereProfileRoot%\bin\stopServer.bat server1
call %WebSphereProfileRoot%\bin\stopServer.bat server1 -username cpeadmin-dv -password P@ssw0rd
echo.

:End
REM pause
