@echo off
REM Restart FileNet P8 AE
cls
title Restart FileNet AE

REM Set the Launch Path
set LaunchPath=%~dp0
set LaunchPath=%LaunchPath:~0,-1%

REM Call Stop FileNet P8 AE
call %LaunchPath%\Stop_FileNet_P8_AE.cmd

REM Call Start FileNet P8 AE
call %LaunchPath%\Start_FileNet_P8_AE.cmd

:End
