'***************************************
'Kill A Process from a Passed Argument
'Filename: KillProcess.vbs
'Created By: Chad J. Hamilton
'Date Created: 11/06/08
'***************************************
Option Explicit

Dim Process,strComputer,objWMIService,colProcessList,objProcess

'Set the Process to kill from the passed in argument
Process = WScript.Arguments.Item(0)

'Set the Computer to the Local Computer
strComputer = "."

Set objWMIService = GetObject("winmgmts:" _
    & "{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2")
Set colProcessList = objWMIService.ExecQuery _
    ("SELECT * FROM Win32_Process WHERE Name = '" & Process & "'")
For Each objProcess in colProcessList
    objProcess.Terminate()
Next
