'*******************************
'Get the FileNet Version
'Filename: GetFileNetVersion.vbs
'Created By: Chad J. Hamilton
'Date Created: 09/16/08
'*******************************
Option Explicit
Dim objShell, colEnvVars, RegResult
Dim strFileNetReg

'Create the Shell object
'Set objShell = CreateObject("WScript.Shell")
Set objShell = WScript.CreateObject("WScript.Shell")

'Create the variable for accessing the Environment Variables
'Set colEnvVars = objShell.Environment("Volatile")

'Set the FNSW Registry Path
strFileNetReg="HKLM\SOFTWARE\FileNET\IMS\CurrentVersion\"

'Set the value of the FNSW directory environment variable
'colEnvVars("FNSWDir") = objShell.RegRead(strFileNetReg & WScript.Arguments(0))

'Set the value of the FNSW_LOC directory environment variable
'colEnvVars("FNSWLOCDir") = objShell.RegRead(strFileNetReg & WScript.Arguments(1))

'RegResult = objShell.RegRead(strFileNetReg & WScript.Arguments(0))
RegResult = objShell.RegRead(strFileNetReg & "Version")

WScript.echo RegResult

'Exit the VBscript
WScript.Quit

