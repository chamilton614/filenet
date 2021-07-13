@echo off
REM Clear FileNet P8 CPE WebSphere Temp Directories
cls
title Clear FileNet P8 CPE WebSphere Temp Directories

REM Set the Launch Path
set LaunchPath=%~dp0
set LaunchPath=%LaunchPath:~0,-1%

REM Call Stop FileNet P8 CPE
REM call %LaunchPath%\Stop_FileNet_P8_CPE.cmd

REM WebSphere Profile Root Path
if exist "C:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=C:\IBM\WebSphere\AppServer\profiles\AppSrv01
if exist "D:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=D:\IBM\WebSphere\AppServer\profiles\AppSrv01

REM Check for the FileNetEngine Temp directory
if exist "%WebSphereProfileRoot%\temp\%computername%Node01\server1\FileNetEngine" (
	echo "%WebSphereProfileRoot%\temp\%computername%Node01\server1\FileNetEngine" exists and will be removed
	echo.
	REM pause
	REM Remove the FileNetEngine directory and all subdirectories
	rmdir /S /Q "%WebSphereProfileRoot%\temp\%computername%Node01\server1\FileNetEngine"
	echo.
) else (
	echo "%WebSphereProfileRoot%\temp\%computername%Node01\server1\FileNetEngine" does not exist
	echo.
)

REM Check for the wstemp directory
setlocal enabledelayedexpansion
if exist "%WebSphereProfileRoot%\wstemp\" (
	echo "%WebSphereProfileRoot%\wstemp\" exists and its contents will be removed
	echo.
	REM pause
	for /F "usebackq" %%i in (`dir /B "%WebSphereProfileRoot%\wstemp\"`) DO (
		echo Deleting "%WebSphereProfileRoot%\wstemp\%%i"
		REM Remove the wstemp contents
		rmdir /S /Q "%WebSphereProfileRoot%\wstemp\%%i"
		echo.
	)
) else (
	echo "%WebSphereProfileRoot%\wstemp" does not exist
	echo.
)
endlocal

REM Call Start FileNet P8 CPE
REM call %LaunchPath%\Start_FileNet_P8_CPE.cmd

pause