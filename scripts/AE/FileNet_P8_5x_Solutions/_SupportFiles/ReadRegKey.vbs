'*******************************
'Read Registry Key Value
'Filename: ReadRegKey.vbs
'Created By: Chad J. Hamilton
'Date Created: 11/28/06
'*******************************
Option Explicit
Dim objShell
Dim strRegPath, strRegKey, Result

'Create the Shell object
Set objShell = CreateObject("WScript.Shell")

'Set the string values from passed in parameters
'If any of the parameters have spaces, you should enclose that parameter within double quotes
'The following is an example:
'strRegPath="HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment"
'strRegKey="Path"

'Passed in Parameter Values
strRegPath = Wscript.Arguments.Item(0)
strRegKey = Wscript.Arguments.Item(1)

'Read the Registry Value based on the passed in parameters
'objShell.RegRead strRegPath & "\" & strRegKey, strRegValue, strRegType
Result=objShell.RegRead (strRegPath & "\" & strRegKey)

'Echo the Result to the Screen
WScript.Echo Result

'Exit the VBscript
WScript.Quit

'End of the script