@echo off
REM Stop SQL Server
cls
title Stop SQL Server

REM Stop SQL Server
echo ===================
echo Stop SQL Server
echo ===================
REM SQL Server Agent
net stop sqlserveragent
echo.

REM SQL Server Browser
net stop sqlbrowser
echo.

REM SQL Server Writer
net stop sqlwriter
echo.

REM SQL Server Full Text Daemon Launcher
net stop mssqlfdlauncher
echo.

REM SQL Server Integration Services
net stop MsDtsServer110

REM SQL Server
net stop mssqlserver
echo.

:End
REM pause
