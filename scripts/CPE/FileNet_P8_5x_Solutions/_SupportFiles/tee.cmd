::this script does not support the ScriptDebug variable.  Enabling script debug is not useful for any scripts which call tee, because most screen output is from the actual tee command.  To debug tee itself, enable the TeeDebug variable.

@if not defined TeeDebug @ECHO OFF
:: Check Windows version
IF NOT "%OS%"=="Windows_NT" GOTO Syntax

:: Keep variables local
SETLOCAL
pushd %~dp0

:: Check command line arguments
SET Append=0
IF /I [%1]==[-a] (
	SET Append=1
	SHIFT
)
IF     [%1]==[] GOTO Syntax
IF NOT [%2]==[] GOTO Syntax

::wrap the filename in quotes, and switch the slashes in case we were passed a variable with them reversed
set TVAR=%1
set File=%TVAR:/=\%
::strip off any quotes.  We only want to add quotes to files that don't already have them.  This is hard to test for, by stripping the quotes we guarantee that when we add new quotes we are adding the only set.
set File=%File:"=%
set File="%File%"

:: Test for invalid wildcards
SET Counter=0
FOR /F %%A IN ('DIR /A /B %File% 2^>NUL') DO CALL :Count "%%~fA"
IF %Counter% GTR 1 (
	SET Counter=
	GOTO Syntax
)

:: Check if a directory with the specified name exists
:: if the directory does not exist, create it
if not exist %~dp1 (
   mkdir %~dp1
   set Append=1
   > %~dp1\tee.log ECHO.Directory %~dp1 did not exist when %~n0 was called.  Created directory at %TIME% on %DATE%.
   > %File% ECHO.Directory %~dp1 did not exist when %~n0 was called.  Created directory at %TIME% on %DATE%.
   > CON ECHO.Notice: Directory %~dp1 did not exist when %~n0 was called.  Created directory at %TIME% on %DATE%.
)
if not errorlevel==0 goto DirectoryCreationError

:: Specify /Y switch for Windows 2000 / XP COPY command
SET Y=
VER | FIND "Windows NT" > NUL
IF ERRORLEVEL 1 SET Y=/Y

:: Flush existing file or create new one if -a wasn't specified
IF %Append%==0 (
   COPY %Y% NUL %File% > NUL 2>&1
)

set TRACEOUT=NUL
if "x%InfraTraceLevel%"=="x1" (
        if not "x%InfraTraceFile%"=="x" (
            set TRACEOUT=%InfraTraceFile%
	)
)

:: Actual TEE
::FOR /F "tokens=1* delims=]" %%A IN ('FIND /N /V ""') DO (
::	>  CON    ECHO.%%B
::	>> %File% ECHO.%%B
::	ECHO %DATE% %TIME% %File%    %%B >> %TRACEOUT%
::)

::cscript //nologo .\vbs\tee.vbs %File% %TRACEOUT%

.\tee.exe %File% %TRACEOUT%

popd

:: Done
ENDLOCAL
GOTO:EOF

:Count
SET /A Counter += 1
GOTO:EOF

:DirectoryCreationError
echo ERROR creating %~dp1
exit /b /1

:Syntax
ECHO.
ECHO Tee.bat,  Version 2.10 for Windows NT 4 / 2000 / XP
ECHO Display text on screen and redirect it to a file simultaneously
ECHO.
ECHO Usage:  some_command  ³  TEE.BAT  [ -a ]  filename
ECHO.
ECHO Where:  "some_command" is the command whose output should be redirected
ECHO         "filename"     is the file the output should be redirected to
ECHO         -a             appends the output of the command to the file,
ECHO                        rather than overwriting the file
ECHO.





