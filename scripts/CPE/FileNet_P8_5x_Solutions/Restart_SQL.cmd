@echo off
REM Restart SQL
cls
title Restart SQL

REM Set the Launch Path
set LaunchPath=%~dp0
set LaunchPath=%LaunchPath:~0,-1%

REM Call Stop SQL
call %LaunchPath%\Stop_SQL_Only.cmd

REM Call Start SQL
call %LaunchPath%\Start_SQL_Only.cmd

:End