'This script will wait for a process to finish running.  If the process is not
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
Dim strProcDeleted     'time process deleted
Dim strProcCreated     'time process created
Dim strComputer        'computer name
Dim strProcessName     'process name
Dim strPID             'process PID
Dim strCSName          'computer name
Dim strProcessFound    'like a boolean, process is found is true, process not found false
Dim objWMIService
Dim colProcesses
Dim objProcess
Dim objLatestEvent
Dim colMonitorProcess
Dim intSecs
Dim strData
Dim arrHMS

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

If Not wscript.arguments.count = 2 Then
  wscript.echo "The arguments passed are invalid."
  wscript.echo "This script requrest two arguments passed."
  wscript.echo "The first argument should be either the letter 'P' or 'N' standing"
  wscript.echo "for PID and Name respectively."
  wscript.echo "The second argument is to be either the PID number or the process name."
  wscript.quit(1)
End If

If Not (UCase(wscript.arguments(0)) = "P" Or UCase(wscript.arguments(0)) = "N") Then
  wscript.echo "The first argument passed is not valid."
  wscript.echo "The first argument should be either the letter 'P' or 'N' standing"
  wscript.echo "for PID and Name respectively."
  wscript.quit(1)
End If

strComputer = "."
strProcess = wscript.arguments(1)
strNameSpace = "root\cimv2"
Set objWMIService = GetObject("winmgmts:" & "{impersonationLevel=impersonate}!\\" & strComputer & "\" & strNameSpace)
If Err.Number <> 0 Then
  wscript.echo "Error occured while trying to get WMI object."
  wscript.echo "Error description returned: " & err.description
  wscript.echo "Error number returned: " & err.number
  wscript.quit(1)
End If

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
'Branch here, where we will be looking for either the PID or the process name
Select Case UCase(wscript.arguments(0))
  Case "P"
    'First make sure the process is running.
    For Each objProcess In colProcesses
      'There seems to be a 'Type' mismatch between objProcess.ProcessID and strProcess
      'To get around this the famous quadruple quotes are used.  If 2780 was the PID,
      'the quadruple quotes add up to 2780 surround in double quotes, ie. "2780"
      If ("""" & objProcess.ProcessID & """") = ("""" & strProcess & """") Then
        strProcessFound = "True"
        wscript.echo "Process " & strProcess & " is found to be running."
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
    Set colMonitorProcess = objWMIService.ExecNotificationQuery _
    ("SELECT * FROM __InstanceDeletionEvent " & _ 
    "WITHIN 1 WHERE TargetInstance ISA 'Win32_Process' " & _
    "AND TargetInstance.ProcessId = '" & strProcess & "'")
  Case "N"
    'First make sure the process is running.
    For Each objProcess In colProcesses
      If objProcess.Name = strProcess Then
        strProcessFound = "True"
        wscript.echo "Process " & strProcess & " is found to be running."
      End If
    Next
    If StrProcessFound = "False" Then
      wscript.echo "Process " & strProcess & " is not found to be running."
      wscript.quit(111)
    End If
    If Err.Number <> 0 Then
      wscript.echo "Error occured while trying to find PID " & strProcess & "."
      wscript.echo "Error description returned: " & err.description
      wscript.echo "Error number returned: " & err.number
      wscript.quit(1)
    End If
    Set colMonitorProcess = objWMIService.ExecNotificationQuery _
    ("SELECT * FROM __InstanceDeletionEvent " & _ 
    "WITHIN 1 WHERE TargetInstance ISA 'Win32_Process' " & _
    "AND TargetInstance.Name = '" & strProcess & "'")
End Select
'Output
WScript.Echo "Waiting for process to stop ..."
Set objLatestEvent = colMonitorProcess.NextEvent
strProcDeleted = Now
strProcCreated = WMIDateToString(objLatestEvent.TargetInstance.CreationDate)
strProcessName = objLatestEvent.TargetInstance.Name
strPID = objLatestEvent.TargetInstance.ProcessId
strCSName = objLatestEvent.TargetInstance.CSName
intSecs = DateDiff("s", strProcCreated, strProcDeleted)
arrHMS = SecsToHours(intSecs)			
strData = "Computer Name: " & strCSName & VbCrLf & _
"  Process Name: " & strProcessName & VbCrLf & _
"  Process ID: " & strPID & VbCrLf & _
"  Time Created: " & strProcCreated & VbCrLf & _
"  Time Deleted: " & strProcDeleted & VbCrLf & _
"  Duration: " & arrHMS(2) & " hours, " & _
arrHMS(1) & " minutes, " & arrHMS(0) & " seconds"		
Wscript.Echo strData


'********************************************************************
'*
'* Private Function WMIDateToString
'*
'* Purpose: Converts the date to a more readable string
'* Input:   dtmDate            objLatestEvent.TargetInstance.CreationDate
'*
'* Output:  dtmDate
'*
'********************************************************************

Private Function WMIDateToString(ByRef dtmDate)
'Convert WMI DATETIME format to US-style date string.

WMIDateToString = CDate(Mid(dtmDate, 5, 2) & "/" & _
                  Mid(dtmDate, 7, 2) & "/" & _
                  Left(dtmDate, 4) & " " & _
                  Mid(dtmDate, 9, 2) & ":" & _
                  Mid(dtmDate, 11, 2) & ":" & _
                  Mid(dtmDate, 13, 2))

End Function
'********************************************************************
'*
'* Private Function SecsToHours
'*
'* Purpose: Convert time in seconds to hours, minutes, seconds and return in array.
'* Input:   intTotalSecs            total number of seconds
'*
'* Output:  SecsToHours							array with seconds minutes and hours
'*
'********************************************************************

Private Function SecsToHours(ByRef intTotalSecs)
'Convert time in seconds to hours, minutes, seconds and return in array.
Dim intHours
Dim intMinutes
Dim intSeconds

intHours = intTotalSecs \ 3600
intMinutes = (intTotalSecs Mod 3600) \ 60
intSeconds = intTotalSecs Mod 60

SecsToHours = Array(intSeconds, intMinutes, intHours)

End Function
