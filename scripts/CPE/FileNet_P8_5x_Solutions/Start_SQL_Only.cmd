@echo off
REM Start SQL Server
cls
title Start SQL Server

REM Start SQL Server
echo ===================
echo Start SQL Server
echo ===================
REM SQL Server
net start mssqlserver
echo.

REM SQL Server Agent
net start sqlserveragent
echo.

REM SQL Server Browser
net start sqlbrowser
echo.

REM SQL Server Writer
net start sqlwriter
echo.

REM SQL Server Full Text Daemon Launcher
net start mssqlfdlauncher
echo.

REM SQL Server Integration Services
net start MsDtsServer110

:End
REM pause

