dim fso
dim sScript
Dim Wshell
Set Wshell = WScript.CreateObject("WScript.Shell")
set fso = CreateObject("Scripting.FileSystemObject")
sScript = fso.getfilename(wscript.fullname)
'WScript.Echo "Starting Directory = " & Wshell.CurrentDirectory

' This script must be executed using cscript.
if ucase(sScript) = "WSCRIPT.EXE" then
    wscript.echo "Error: You must run this script using cscript.exe"
else
	main
end if

'*************************************************************************
'* Main Program Logic Starts Here                                        *
'*************************************************************************

'*************************************************************************
'* Validate the command line arguments and call ReplaceTokensInFile      *
'*************************************************************************
sub main()
    dim oArgs
    dim fso
    dim bStatus

    set fso = CreateObject("Scripting.FileSystemObject")
    set oArgs = ParseArgs

    if oArgs.count <> 2 then
       wscript.echo "Error: Invalid arguments."
       wscript.echo "       Example: cscript envfile.vbs /s=SourceFile /d=DestFile"
       exit sub
    end if

    if (oargs("/s") = "") then
        wscript.echo "Error: Invalid arguments the source file name is required."
        exit sub
    end if

    if (oargs("/d") = "") then
        wscript.echo "Error: Invalid arguments the destination file name is required."
        exit sub
    end if

    if not (fso.fileexists(oargs("/s"))) then
        wscript.echo "Error: The source file does not exist."
	wscript.echo "       " & oargs("/s")
        exit sub
    end if

    bStatus = ReplaceTokensInFile(oargs("/s"), oargs("/d"))

end sub

'*************************************************************************
'* Functions start here                                                  *
'*************************************************************************

'*************************************************************************
'* Create a Dictionary Object to hold our command line arguments         *
'*************************************************************************
function ParseArgs()
	On Error Resume Next
     Dim g_oSwitches
     Set g_oSwitches = CreateObject("Scripting.Dictionary")
     Dim pair, list, sArg, Item
     For Each sArg In Wscript.Arguments
          pair = Split(sArg, "=", 2)

          'if value is specified multiple times, last one wins
          If g_oSwitches.Exists(Trim(pair(0))) Then
               g_oSwitches.Remove(Trim(pair(0)))
          End If

          If UBound(pair) >= 1 Then
               g_oSwitches.add Trim(pair(0)), Trim(pair(1))
          Else
               g_oSwitches.add Trim(pair(0)),""
          End If
     Next
     Set ParseArgs = g_oSwitches
End function

'*************************************************************************
'* Read the source file into a buffer                                    *
'* Pass the buffer to ReplaceTokens                                      *
'* Write the boffer to the Destination file If it contains any data      *
'*************************************************************************
Function ReplaceTokensInFile(sSourceFile, sDestFile)
    On Error Resume Next
    Const ForReading = 1, ForWriting = 2, ForAppending = 8
    Const TristateUseDefault = -2, TristateTrue = -1, TristateFalse = 0
    Dim sTempFile
    Dim sTXStream
    Dim sBuffer
    Dim sStatus
    Dim sStarTeamPath
    dem fso

    set fso = CreateObject("Scripting.FileSystemObject")
    
    'Open the File
    If fso.FileExists(sSourceFile) Then
        Set sTempFile = fso.GetFile(sSourceFile)
        Set sTXStream = sTempFile.OpenAsTextStream(ForReading, TristateUseDefault)
        
        ' Read the entire file into a buffer
        sBuffer = sTXStream.ReadAll
        
        sTXStream.Close

        ' Replace known tokens in the file
        sBuffer = ReplaceTokens(sBuffer)
        ' Taken out to use the ReplaceTokens function
        ' sBuffer = wShell.ExpandEnvironmentStrings(sBuffer)
        wscript.echo "************************************************************"
	wscript.echo "Replaced Token File"
        wscript.echo "************************************************************"
	wscript.echo sBuffer

	if trim(sBuffer) <> "" then
            'Write it back out
            fso.DeleteFile (sDestFile)
            Set sTXStream = fso.CreateTextFile(sDestFile, True, False)
            'Set sTXStream = sTempFile.OpenAsTextStream(ForWriting, TristateUseDefault)
            sTXStream.Write (sBuffer)
            If Err.Number <> 0 Then
                sTXStream.Close
                ReplaceTokensInFile = False
                Exit Function
            Else
                sTXStream.Close
            End If
        else
            ReplaceTokensInFile = False
            Exit Function
        end if
    Else
        wscript.echo "Error: " & sSourceFile & " does not exists.", ""
        ReplaceTokensInFile = False
        Exit Function
    End If
    ReplaceTokensInFile = True
End Function

Function ReplaceTokens(sSource)
    On Error Resume Next
    Dim lStart
    Dim lStop
    Dim sToken
    Dim sValue
    dim Wshell

    set WShell = WScript.CreateObject("WScript.Shell")

    lStart = InStr(1, sSource, "%", vbTextCompare)
    lStop = InStr(lStart + 1, sSource, "%", vbTextCompare)
    While lStart <> 0 And lStop > lStart And lStop <> 0
    	If Mid(sSource, lStart + 1,1) <> " " and _
    	   Mid(sSource, lStart + 1,1) <> """" Then
        	If lStart > 0 And lStop > lStart Then
	            	sToken = Mid(sSource, lStart, lStop - lStart + 1)
		    	wscript.echo " "
		    	wscript.echo "Token = " & sToken
		    	if sToken <> "" then
	                	sValue = WShell.ExpandEnvironmentStrings(trim(sToken))        	
				wscript.echo "	Token Value = " & sValue
	                	If sValue <> "" Then sSource = Replace(sSource, sToken, sValue, 1, -1, vbTextCompare)
	                	if svalue = "" then 
	        	            wscript.echo "Error: Token value is undefined in the current Environment."
		                end if
			    else
		                wscript.echo "Error: Token " & sToken & " was not found."
	        	    end if
		        End If
	        End If
        lStart = InStr(lStart + len(svalue), sSource, "%", vbTextCompare)
        lStop = InStr(lStart + 1, sSource, "%", vbTextCompare)
    Wend
    ReplaceTokens = sSource
End Function
