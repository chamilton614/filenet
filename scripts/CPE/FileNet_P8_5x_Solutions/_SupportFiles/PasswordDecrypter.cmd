REM ===============================================================================================
REM Name:       Password Decrypter Script
REM Script:     PasswordDecrypter.cmd
REM Created By: FileNet Solutions Team
REM ===============================================================================================
@echo off
title Password Decrypter
cls

REM ========================================
REM START SET VARIABLES
REM ========================================
:SetVariables
REM Set the Debug Flag - NoPause=0, Pause=1
set Debug=1

REM Set the Reboot Flag - NoReboot=0, Reboot=1
set Reboot=0

REM Set the Passed in Password Parameter
set PasswordReceived=%1

echo Password Input: %PasswordReceived%

REM Set the Launch Path
set LaunchPath=%~dp0%
set LaunchPath=%LaunchPath:~0,-1%

REM Set the Java Home
set JavaHome=%LaunchPath%\jre1.6.0_06\bin
REM Support Files Path
REM set SupportFilesPath=%LaunchPath%\..\..\_SupportFiles

REM Ensure Crypto.jar is available - only needed if %RemoteBackupFileLocation% is
REM defined triggering a connection to a remote location.
if not exist "%LaunchPath%\Crypto.jar" (
	echo **** ERROR: "%LaunchPath%\Crypto.jar" is not available to decrypt passwords.
    echo **** ERROR: Cannot connect to %RemoteBackupFileLocation% to copy backup files - terminating script.
    goto :ExitWithErrors
)

REM Need to connect to the %RemoteBackupFileLocation%
echo Removing any existing connections.

REM Remove any currently mapped connection as it may use other credentials
net use "%RemoteBackupFileLocation%" /delete /y 2>nul
echo Attempting to connect to: "%RemoteBackupFileLocation%".

REM Connect to the backup server.  The password variable is decrypted within the command section of the 
REM for loop so it only exists within the scope of the loop.
for /f %%i in ('%JavaHome%\java -cp "%LaunchPath%\Crypto.jar" SimpleCryptographyMgr -decrypt "%PasswordReceived%"') do (
	REM Connect to the backup server
	REM net use "%RemoteBackupFileLocation%" /user:%remoteBackupFileServerUserID% %%b > nul
	set PasswordToUse=%%i
)


REM Verify the mapping was successful
if exist "%RemoteBackupFileLocation%\." (
    echo            Successfully connected to "%RemoteBackupFileLocation%".
) else (
    echo            **** ERROR: Failed to connect to "%RemoteBackupFileLocation%".
    echo            **** ERROR: Can not copy backup files - terminating script.
    goto :ExitWithErrors
)

