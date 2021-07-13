@echo off
REM Start FileNet P8 Environment
cls
title Start FileNet P8 Environment

REM Set the Launch Path
set LaunchPath=%~dp0
set LaunchPath=%LaunchPath:~0,-1%

REM Call Start SQL Only
call %LaunchPath%\Start_SQL_Only.cmd

REM Call Start FileNet P8 CPE
call %LaunchPath%\Start_FileNet_P8_CPE.cmd

:End
REM pause