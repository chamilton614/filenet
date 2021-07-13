' Usage: sleep <number of seconds>
dim fso
dim sScript
Dim Wshell
Set Wshell = WScript.CreateObject("WScript.Shell")
set fso = CreateObject("Scripting.FileSystemObject")
sScript = fso.getfilename(wscript.fullname)
dim currentPID

' This script must be executed using cscript.
if ucase(sScript) = "WSCRIPT.EXE" then
    wscript.echo "Error: You must run this script using cscript.exe"
else
    For Each sArg In Wscript.Arguments
	wscript.sleep sArg*1000
    next
end if