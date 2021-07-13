@echo off
REM Stop FileNet P8 Environment
cls
title Stop FileNet P8 Environment

REM Set the Launch Path
set LaunchPath=%~dp0
set LaunchPath=%LaunchPath:~0,-1%

REM Call Stop FileNet P8 CPE
call %LaunchPath%\Stop_FileNet_P8_CPE.cmd

REM Call Stop SQL Only
call %LaunchPath%\Stop_SQL_Only.cmd

:End
REM pause