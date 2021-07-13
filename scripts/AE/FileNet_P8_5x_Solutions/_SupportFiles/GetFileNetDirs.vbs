'*******************************
'Get the FileNet Directories
'Filename: GetFileNetDirs.vbs
'Created By: Chad J. Hamilton
'Date Created: 09/16/08
'*******************************
Option Explicit
Dim objShell, RegResult1, RegResult2
Dim strFileNetReg

'Create the Shell object
'Set objShell = CreateObject("WScript.Shell")
Set objShell = WScript.CreateObject("WScript.Shell")

'Set the FNSW Registry Path
strFileNetReg="HKLM\SOFTWARE\FileNET\IMS\CurrentVersion\"

'RegResult = objShell.RegRead(strFileNetReg & WScript.Arguments(0))
RegResult1 = objShell.RegRead(strFileNetReg & "FNSW_LIB")
RegResult2 = objShell.RegRead(strFileNetReg & "FNSW_LOCAL")

'Output the value of the FNSW Directory
WScript.echo RegResult1
'Output the value of the FNSW_LOC Directory
WScript.echo RegResult2

'Exit the VBscript
WScript.Quit

