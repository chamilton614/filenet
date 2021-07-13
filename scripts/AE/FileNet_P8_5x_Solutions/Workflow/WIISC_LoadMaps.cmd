@echo off

REM Get the Passed in Parameters
REM set ServerURL=%~1
REM set ServerPort=%~2
REM set ServerProtocol=%~3

REM Local Use values not passed in
set ServerURL=tstp8521aevm.tstp8.com
set ServerPort=9080
set ServerProtocol=http

REM Get the Launch Path
set LaunchPath=%~dp0

REM Support Files Path
set SupportFiles=%LaunchPath%\..\_SupportFiles

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
set WIISC_URL=%ServerProtocol%://%ServerURL%:%ServerPort%/WIISC/Workflow/LoadMaps

REM Launch Command
echo =====================================================
echo 	Loading Workflow Maps
echo =====================================================
powershell -f "%SupportFiles%\PostAuthWebPage.ps1" "%WIISCUsername%" "%WIISCPassword%" "%WIISC_URL%"
if ERRORLEVEL 1 exit /b %ErrorLevel%
if ERRORLEVEL 0 echo Loading Workflow Maps Successful!
echo.
echo.

:End
pause
exit /b 0