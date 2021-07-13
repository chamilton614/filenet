@echo off
REM Clear FileNet P8 AE WebSphere Temp Directories
cls
title Clear FileNet P8 AE WebSphere Temp Directories

REM Set the Launch Path
set LaunchPath=%~dp0
set LaunchPath=%LaunchPath:~0,-1%

REM Call Stop FileNet P8 AE
REM call %LaunchPath%\Stop_FileNet_P8_AE.cmd

REM WebSphere Profile Root Path
if exist "C:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=C:\IBM\WebSphere\AppServer\profiles\AppSrv01
if exist "D:\IBM\WebSphere\AppServer\profiles\AppSrv01" set WebSphereProfileRoot=D:\IBM\WebSphere\AppServer\profiles\AppSrv01

REM Check for the WorkplaceXT Temp directory
if exist "%WebSphereProfileRoot%\temp\%computername%Node01\server1\WorkplaceXT" (
	echo "%WebSphereProfileRoot%\temp\%computername%Node01\server1\WorkplaceXT" exists and will be removed
	echo.
	REM pause
	REM Remove the WorkplaceXT directory and all subdirectories
	rmdir /S /Q "%WebSphereProfileRoot%\temp\%computername%Node01\server1\WorkplaceXT"
	echo.
) else (
	echo "%WebSphereProfileRoot%\temp\%computername%Node01\server1\WorkplaceXT" does not exist
	echo.
)

REM Check for the navigator Temp directory
if exist "%WebSphereProfileRoot%\temp\%computername%Node01\server1\navigator" (
	echo "%WebSphereProfileRoot%\temp\%computername%Node01\server1\navigator" exists and will be removed
	echo.
	REM pause
	REM Remove the navigator directory and all subdirectories
	rmdir /S /Q "%WebSphereProfileRoot%\temp\%computername%Node01\server1\navigator"
	echo.
) else (
	echo "%WebSphereProfileRoot%\temp\%computername%Node01\server1\navigator" does not exist
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

REM Call Start FileNet P8 AE
REM call %LaunchPath%\Start_FileNet_P8_AE.cmd

pause