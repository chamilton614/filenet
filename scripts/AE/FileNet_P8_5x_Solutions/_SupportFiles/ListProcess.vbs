'********************************************************************************
'* ListProcess.vbs 
'* 
'* This script will display the following attributes all services and running
'* processes.  The attributes will be tab seperated.
'*
'* Type (Service or Process)
'* Process ID
'* Service/Process Name
'* Service/PProcess Command line
'* Service/Process Status
'* 
'* Input Parms: (Optional) Computer Name, defaults to the local machine
'* Returns: ERRORLEVEL
'********************************************************************************
Dim arg
Dim strComputer
Dim objWMIService
Dim Services, Service
Dim Processes, Process

'Default to the current computer name
strComputer = "."

'Check for passed in arguments
If WScript.Arguments.Count > 1 Then
	WScript.Echo  "ERROR: The only valid command line argument is the target computer name."
	wscript.quit(1)
elseif wscript.arguments.count = 1 then 
	'Set the computer name to the passed in value
	strComputer = wscript.arguments(0)
end if

on error resume next

'Connect to the target computer
Set objWMIService = GetObject("winmgmts:" _
    & "{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2")
if err.number <> 0 then wscript.quit (1)

' Get the list of services from the target computer
Set Services = objWMIService. _        
    ExecQuery("select * from Win32_Service")
for each service in Services
    Wscript.Echo "Service" & vbtab & service.processid & vbtab & service.Name & vbtab & service.PathName & vbtab & service.state
next

' Get the list of processes from the target computer
Set Processes = objWMIService. _        
    ExecQuery("select * from Win32_Process")
for each process in Processes 
    Wscript.Echo "Process" & vbtab & process.processid & vbtab & process.Name & vbtab & process.commandline & vbtab & "Running"
next


