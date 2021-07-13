REM ==========================================================================================================
REM Script Name: SendEmail.cmd
REM Description: This script will send an email as long as a Subject, Body and Email Addreses are supplied.
REM              There is also the ability to attach a file to the email as well.
REM ==========================================================================================================
@echo off
title Send Email
cls

REM Set Variables
set Subject=
set Body=
set EmailAddress=

REM Launch SendEmail.vbs
cscript.exe //NoLogo .\SendEmail.vbs %Subject% %Body% %EmailAddress%

:End
