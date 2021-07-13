REM Script:
REM GetDateTime.cmd
REM 
REM Description:
REM This Script is used to get the Current Date and Time.
REM 
REM Variables Output: 
REM 	Date1: mm-dd-yyyy
REM 	Time1: hhmm_am or pm
REM
REM Created By:
REM Chad J. Hamilton
REM *****************************************************************************************
@echo off

REM Get the Current Date and Time
for /F "usebackq Tokens=2,3,4 Delims=/ " %%j in (`date /T`) DO (
	set Date1=%%j-%%k-%%l
	set Date2=%%l%%j%%k
)
for /F "usebackq Tokens=1,2,3 Delims=: " %%a in (`time /T`) DO (
	set Time1=%%a%%b_%%c
)

:End
