@echo off
REM Restart FileNet P8 CPE
cls
title Restart FileNet CPE

REM Set the Launch Path
set LaunchPath=%~dp0
set LaunchPath=%LaunchPath:~0,-1%

REM Call Stop FileNet P8 CPE
call %LaunchPath%\Stop_FileNet_P8_CPE.cmd

REM Call Start FileNet P8 CPE
call %LaunchPath%\Start_FileNet_P8_CPE.cmd

:End
