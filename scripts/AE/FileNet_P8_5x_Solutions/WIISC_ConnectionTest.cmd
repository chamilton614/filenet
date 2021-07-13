@echo off

REM Get the Passed in Parameters
REM set ServerURL=%~1
REM set ServerPort=%~2
REM set ServerProtocol=%~3

REM Get the Launch Path
set LaunchPath=%~dp0

REM Support Files Path
set SupportFiles=%LaunchPath%\_SupportFiles

REM Imaging Connection Test URL
set ImagingURL=http://tstp8521aevm.tstp8.com:9080/WIISC/Imaging/ConnectionTest

REM Workflow Connection Test URL
set WorkflowURL=http://tstp8521aevm.tstp8.com:9080/WIISC/Workflow/ConnectionTest

REM WIISC Username
set WIISCUsername=admin

REM WIISC Password
set WIISCPassword=P@ssw0rd

REM Run Set Environment
REM if exist "%LaunchPath%\..\..\..\Scripts\SetEnv.bat" call "%LaunchPath%\..\..\..\Scripts\SetEnv.bat"
REM if exist "%LaunchPath%\..\..\..\Scripts\GetWebSpherePorts.cmd" call "%LaunchPath%\..\..\..\Scripts\GetWebSpherePorts.cmd"

REM Check the Parameters
REM if "%ServerURL%"=="" set ServerURL=%ComputerName%
REM if "%ServerPort%"=="" set ServerPort=%WebSphereHTTPPort%
REM if "%ServerProtocol%"=="" set ServerProtocol=http

REM Get the WIISC URL
REM set WIISC_URL=%ServerProtocol%://%ServerURL%:%ServerPort%/WIISC/Workflow/LoadMaps

REM Launch Command
echo =====================================================
echo 	Imaging Connection Test
echo =====================================================
powershell -f "%SupportFiles%\PostAuthWebPage.ps1" "%WIISCUsername%" "%WIISCPassword%" "%ImagingURL%"
if ERRORLEVEL 1 exit /b %ErrorLevel%
if ERRORLEVEL 0 echo Connection Successful!
echo.
echo.
echo =====================================================
echo 	Workflow Connection Test
echo =====================================================
powershell -f "%SupportFiles%\PostAuthWebPage.ps1" "%WIISCUsername%" "%WIISCPassword%" "%WorkflowURL%"
if ERRORLEVEL 1 exit /b %ErrorLevel%
if ERRORLEVEL 0 echo Connection Successful!
echo.

:End
pause
exit /b 0