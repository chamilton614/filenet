'This script will output three columns, process name, pid and memory usage.  If the process is not
'found to be running it will exit with an error code of 111 rather than the typical
'1 error code.

Option Explicit
On Error Resume Next

Const	CONST_VBQUOTE     = """"      'required for output

Dim oWshell            'Only used for finding the starting directory
Dim fso                'file system object
Dim sScript            'script host
Dim strProcess         'argument 2, the process name or PID
Dim strNameSpace       'the WBEM name space
Dim strComputer        'computer name
Dim strProcessFound    'like a boolean, process is found is true, process not found false
Dim objWMIService
Dim colProcesses
Dim objProcess

'Check to see if the script is launched with cscript.exe
Set oWshell = WScript.CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
sScript = fso.getfilename(wscript.fullname)
'WScript.Echo "Starting Directory = " & oWshell.CurrentDirectory


'This script must be executed using cscript.
If UCase(sScript) = "WSCRIPT.EXE" Then
  Call WScript.echo ("Error: You must run this script using cscript.exe")
  wscript.quit(1)
End If

'If Not wscript.arguments.count = 2 Then
'  wscript.echo "The arguments passed are invalid."
'  wscript.echo "This script requrest two arguments passed."
'  wscript.echo "The first argument should be either the letter 'P' or 'N' standing"
'  wscript.echo "for PID and Name respectively."
'  wscript.echo "The second argument is to be either the PID number or the process name."
'  wscript.quit(1)
'End If
'
If Not (UCase(wscript.arguments(0)) = "A" Or UCase(wscript.arguments(0)) = "P" Or UCase(wscript.arguments(0)) = "N") Then
  wscript.echo "The first argument passed is not valid."
  wscript.echo "The first argument should be either the letter 'A', 'P' or 'N' standing"
  wscript.echo "for PID and Name respectively."
  wscript.quit(1)
End If

strComputer = "."
If wscript.arguments.count = 2 Then
	strProcess = wscript.arguments(1)
End If
strNameSpace = "root\cimv2"
Set objWMIService = GetObject("winmgmts:" & "{impersonationLevel=impersonate}!\\" & strComputer & "\" & strNameSpace)
If Err.Number <> 0 Then
  wscript.echo "Error occured while trying to get WMI object."
  wscript.echo "Error description returned: " & err.description
  wscript.echo "Error number returned: " & err.number
  wscript.quit(1)
End If

'Branch here, where we will be looking for either the PID or the process name
Select Case UCase(wscript.arguments(0))
  Case "A"
    'Get a list of currently running processes.
    Set colProcesses = objWMIService.ExecQuery("Select * from Win32_Process")
    If Err.Number <> 0 Then
       wscript.echo "Error getting a list of running processes."
       wscript.echo "Error description returned: " & err.description
       wscript.echo "Error number returned: " & err.number
       wscript.quit(1)
    End If
    For Each objProcess In colProcesses
      Call writeOutput(objProcess.Name, objProcess.ProcessID, objProcess.WorkingSetSize)
    Next
    If Err.Number <> 0 Then
      wscript.echo "Error occured while trying to find process."
      wscript.echo "Error description returned: " & err.description
      wscript.echo "Error number returned: " & err.number
      wscript.quit(1)
    End If
  Case "P"
    'Get a list of currently running processes.
    Set colProcesses = objWMIService.ExecQuery("Select * from Win32_Process")
    If Err.Number <> 0 Then
       wscript.echo "Error getting a list of running processes " & strProcess & "."
       wscript.echo "Error description returned: " & err.description
       wscript.echo "Error number returned: " & err.number
       wscript.quit(1)
    End If
    'Initialize strProcess
    strProcessFound = "False"
    'First make sure the process is running.
    For Each objProcess In colProcesses
      'There seems to be a 'Type' mismatch between objProcess.ProcessID and strProcess
      'To get around this the famous quadruple quotes are used.  If 2780 was the PID,
      'the quadruple quotes add up to 2780 surround in double quotes, ie. "2780"
      If ("""" & objProcess.ProcessID & """") = ("""" & strProcess & """") Then
        strProcessFound = "True"
        'WorkingSetSize is memory
        Call writeOutput(objProcess.Name, objProcess.ProcessID, objProcess.WorkingSetSize)
      End If
    Next
    If strProcessFound = "False" Then
      wscript.echo "Process " & strProcess & " is not found to be running."
      wscript.quit(111)
    End If
    If Err.Number <> 0 Then
      wscript.echo "Error occured while trying to find PID " & strProcess & "."
      wscript.echo "Error description returned: " & err.description
      wscript.echo "Error number returned: " & err.number
      wscript.quit(1)
    End If
  Case "N"
    'Get a list of currently running processes.
    Set colProcesses = objWMIService.ExecQuery("Select * from Win32_Process")
    If Err.Number <> 0 Then
       wscript.echo "Error getting a list of running processes " & strProcess & "."
       wscript.echo "Error description returned: " & err.description
       wscript.echo "Error number returned: " & err.number
       wscript.quit(1)
    End If
    'Initialize strProcess
    strProcessFound = "False"
    'First make sure the process is running.
    For Each objProcess In colProcesses
      If objProcess.Name = strProcess Then
        strProcessFound = "True"
        'WorkingSetSize is memory
        Call writeOutput(objProcess.Name, objProcess.ProcessID, objProcess.WorkingSetSize)
      End If
    Next
    If StrProcessFound = "False" Then
     wscript.echo "Process " & strProcess & " is not found to be running."
      wscript.quit(111)
    End If
    If Err.Number <> 0 Then
      wscript.echo "Error occured while trying to find process " & strProcess & "."
      wscript.echo "Error description returned: " & err.description
      wscript.echo "Error number returned: " & err.number
      wscript.quit(1)
    End If
End Select

'********************************************************************
'*
'* Private Sub Output
'*
'* Purpose: Output info
'* Input:   objProcess.Name,objProcess.ProcessID,objProcess.WorkingSetSize
'*
'********************************************************************
Private Sub WriteOutput(strProcessName, strProcessID, strProcessMemory)

'This is very basic to facilitate usage in a command script 'for' loop
strProcessMemory = strProcessMemory / 1024
wscript.echo strProcessName & space(1) & strProcessID & space(1) & strProcessMemory

End sub


