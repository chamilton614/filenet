'********************************************************************
'*
'* File:           SERVICE.VBS
'* Created:        March 1999
'* Version:        1.0
'*
'* Main Function: Controls services on a machine.
'*
'*  1.  Service.vbs /L
'*                 [/S <server>][/U <username>][/W <password>]
'*                 [/O <outputfile>]
'*
'*  2.  Service.vbs /G | /X | /R | /D | /M <StartMode>
'*                  /N <service>
'*                 [/S <server>][/U <username>][/W <password>]
'*                 [/O <outputfile>]
'*
'*  3.  Service.vbs /I /E <execname> /N <service> [/C <DisplayName>]
'*                 [/S <server>][/U <username>][/W <password>] [/d <depend on service name>]
'*                 [/O <outputfile>]
'*
'* Copyright (C) 1999 Microsoft Corporation
'*
'********************************************************************
OPTION EXPLICIT

    'Define constants

    CONST CONST_ERROR                   = 0
    CONST CONST_WSCRIPT                 = 1
    CONST CONST_CSCRIPT                 = 2
    CONST CONST_SHOW_USAGE              = 3
    CONST CONST_PROCEED                 = 4
    CONST CONST_LIST                    = "LIST"
    CONST CONST_START                   = "START"
    CONST CONST_STOP                    = "STOP"
    CONST CONST_INSTALL                 = "INSTALL"
    CONST CONST_UPDATE                  = "UPDATE"
    CONST CONST_REMOVE                  = "REMOVE"
    CONST CONST_DEPENDS                 = "DEPENDS"
    CONST CONST_MODE                    = "MODE"

    Const OWN_PROCESS 			= 16
    Const NOT_INTERACTIVE 		= False
    Const NORMAL_ERROR_CONTROL 		= 2

    'Declare variables
    Dim strOutputFile, intOpMode, i
    Dim strServer, strUserName, strPassword
    Dim strTaskCommand, strServiceName, strExecName
    Dim strStartMode, strDisplayName, strDepends
    Dim bIncludeDepends

    'Make sure the host is csript, if not then abort
    VerifyHostIsCscript()

    'Parse the command line
    intOpMode = intParseCmdLine( strServer      ,  _
                                 strUserName    ,  _
                                 strPassword    ,  _
                                 strOutputFile  ,  _
                                 strTaskCommand ,  _
                                 strServiceName ,  _
                                 strStartMode   ,  _
                                 strDisplayName ,  _
                                 strExecName    ,  _
				 strDepends     )

    Select Case intOpMode
        Case CONST_SHOW_USAGE
            Call ShowUsage()

        Case CONST_PROCEED
            Call Service(strTaskCommand, _
		         strServiceName,    _ 
			 strExecName,       _
   			 strDisplayName,    _
                         strStartMode,      _
			 strServer,         _
                         strOutputFile,     _
			 strUserName,       _
			 strPassword,       _
			 strDepends         )

        Case CONST_ERROR
            Wscript.Echo ("Error occurred in passing parameters.")

        Case Else                    'Default -- should never happen
            call Print("Error occurred in passing parameters.")

    End Select

'********************************************************************
'*
'* Sub Service()
'* Purpose: Controls services on a machine.
'* Input:   
'*          strTaskCommand      one of /list, /start, /stop /install /remove
'*                              /dependents
'*          strServiceName      name of the service
'*          strExecName         name of executable for service install
'*          strDisplayName      Display name for the service.
'*          strStartMode        start mode of the service
'*          strServer           a machine name
'*          strOutputFile       an output file name
'*          strUserName         the current user's name
'*          strPassword         the current user's password
'* Output:  Results are either printed on screen or saved in strOutputFile.
'*
'********************************************************************
Private Sub Service(strTaskCommand,    _ 
                    strServiceName,    _
                    strExecName,       _
                    strDisplayName,    _
                    strStartMode,      _
                    strServer,	       _
                    strOutputFile,     _
	            strUserName,       _
	            strPassword,       _
		    strDepends         )

    ON ERROR RESUME NEXT

    Dim objFileSystem, objOutputFile, objService, strQuery

    'Open a text file for output if the file is requested
    If Not IsEmpty(strOutputFile) Then
        If (NOT blnOpenFile(strOutputFile, objOutputFile)) Then
            Call Wscript.Echo ("Could not open an output file.")
            Exit Sub
        End If
    End If

    'Establish a connection with the server.
    'If blnConnect("root\cimv2" , _
    '               strUserName , _
    '               strPassword , _
    '               strServer   , _
    '               objService  ) Then
    If blnConnect("root\cimv2" , _
                   "" , _
                   "" , _
                   strServer   , _
                   objService  ) Then
        Call Wscript.Echo("")
        Call Wscript.Echo("Please check the server name, " _
                        & "credentials and WBEM Core.")
        Exit Sub
    End If

    'Now execute the method.
    on error goto 0
    Call ExecuteMethod(objService,        _
	               objOutputFile,     _
	               strTaskCommand,    _
	               strServiceName,    _
	               strExecName,       _
    		       strDisplayName,    _
                       strStartMode,      _
		       strDepends         )


    If NOT IsEmpty(objOutputFile) Then
        objOutputFile.Close
        Wscript.Echo "Results are saved in file " & strOutputFile & "."
    End If

End Sub

'********************************************************************
'*
'* Sub ExecMethod()
'* Purpose: Executes a method.
'* Input:   objService          a service object
'*          objOutputFile       an output file object
'*          strTaskCommand   one of /list, /start, /stop /install /delete
'*          strServiceName      name of the service to be started or stopped
'*          strExecName         name of executable for service install
'* 		    strDisplayName,     Display name for the service.
'*          strStartMode        start mode of the service
'* Output:  Results are either printed on screen or saved in objOutputFile.
'*
'********************************************************************
Private Sub ExecuteMethod(objService,        _
                          objOutputFile,     _
                          strTaskCommand,    _
                          strServiceName,    _
			  strExecName,       _
                          strDisplayName,    _
                          strStartMode,      _
			  strDepends         )

    ON ERROR RESUME NEXT

    Dim objEnumerator, objInstance, strMessage, intStatus, objReference, result
    ReDim strName(0), strDisplay(0), strState(0), intOrder(0)

	'Initialize local variables
    strMessage        = ""
    strName(0)        = ""
    strDisplay(0) = ""
    strState(0)       = ""
    intOrder(0)       = 0

    Select Case strTaskCommand
        Case CONST_START
            Set objInstance = objService.Get("Win32_Service='" &_
                strServiceName & "'")
            If Err.Number Then
                call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                    " occurred in getting " & _
                      "service " & strServiceName & ".")
                If Err.Description <> "" Then
                    call Print( "Error description: " & Err.Description & ".")
                End If
                Err.Clear
                Exit Sub
            End If
            If objInstance is nothing Then
                Exit Sub
            Else
		If objInstance.State <> "Running" Then
	                intStatus = objInstance.StartService()
			strMessage = DisplayStatus("Start",strServiceName,intStatus)
	                result = vbTrue
	                result = WaitStatus (objService, strServiceName, "Running", 300)
	                intStatus = objInstance.StartService()
			strMessage = DisplayStatus("Start",strServiceName,intStatus)
		End If
                WriteLine strMessage, objOutputFile
            End If

        Case CONST_STOP
            Set objInstance = objService.Get("Win32_Service='" & _
                 strServiceName&"'")
            If Err.Number Then
                call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                    " occurred in getting " & _
                      "service " & strServiceName & ".")
                Err.Clear
                Exit Sub
            End If
            If objInstance is nothing Then
                Exit Sub
            Else
		If objInstance.State <> "Stopped" Then
	                intStatus = objInstance.StopService()
			strMessage = DisplayStatus("Stop",strServiceName,intStatus)
	                result = WaitStatus (objService, strServiceName, "Stopped", 300)
	                intStatus = objInstance.StopService()
			strMessage = DisplayStatus("Stop",strServiceName,intStatus)
	        End If
                WriteLine strMessage, objOutputFile
            End If

        Case CONST_MODE

            Set objInstance = objService.Get("Win32_Service='" & _
                strServiceName & "'")
            If Err.Number Then
                call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                    " occurred in getting " & _
                    "service " & strServiceName & ".")
                Err.Clear
                Exit Sub
            End If
            If objInstance is nothing Then
                Exit Sub
            Else
                intStatus = objInstance.ChangeStartMode(strStartMode)
		strMessage = DisplayStatus("ChangeStartMode",strServiceName,intStatus)
                WriteLine strMessage, objOutputFile

                strDisplayName = objInstance.DisplayName
                strExeName = objInstance.PathName
		If IsEmpty(strUserName) then strUserName = objInstance.StartName
		If IsEmpty(strPassword) then 
		    strPassword = null
		end if

                intStatus = objInstance.Change( , _
						, _
						, _
						, _
						, _
						, _
						strUserName, _
						strPassword)
		strMessage = DisplayStatus("Change",strServiceName,intStatus)
                WriteLine strMessage, objOutputFile
            End If

        Case CONST_INSTALL
            Set objInstance = objService.Get("Win32_BaseService")
            If Err.Number Then
                call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                " occurred in getting " & _
                      "service " & strServiceName & ".")
                If Err.Description <> "" Then
                    call Print( "Error description: " & Err.Description & ".")
                End If
                Err.Clear
                Exit Sub
            End If
            If objInstance is Nothing Then
                Exit Sub
            Else

                If IsEmpty(strDisplayName) then strDisplayName = strServiceName
		if isEmpty(strStartMode) then strStartMode = "Manual"
		if isEmpty(strUserName) then 
		    strUserName = null
		    strPassword = null
		end if
		
		Dim aDepends(0)
		if strDepends <> "" then 
			dim i, strDep
			i=0
			for each strDep in split(strDepends,",")
				if i > 0 then redim Preserve aDepends(i)
				aDepends(i) = strDep
				i=i+1
			next
		end if

		intStatus = objInstance.Create( strServiceName, _
						strDisplayName, _
						strExecName, _
					        OWN_PROCESS, _
						NORMAL_ERROR_CONTROL, _
						strStartMode, _
						NOT_INTERACTIVE, _
						strUserName, _
					        strPassword, _
						null,          _
						null,          _
						aDepends)
		strMessage = DisplayStatus("Create",strServiceName,intStatus)
                WriteLine strMessage, objOutputFile
            End If

        Case CONST_REMOVE
            Set objInstance = objService.Get("Win32_Service='" & _
                strServiceName & "'")
            If Err.Number Then
                call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                    " occurred in getting " & _
                    "service " & strServiceName & ".")
                If Err.Description <> "" Then
                    call Print( "Error description: " & Err.Description & ".")
                End If
                Err.Clear
                Exit Sub
            End If
            If objInstance is Nothing Then
                Exit Sub
            Else
                intStatus = objInstance.Delete()
		strMessage = DisplayStatus("Delete",strServiceName,intStatus)
                WriteLine strMessage, objOutputFile
            End If

        Case CONST_DEPENDS
            Set objInstance = objService.Get("Win32_Service='" & _
                strServiceName&"'")
            If Err.Number Then
                call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                    " occurred in getting " & _
                      "service " & strServiceName & ".")
                If Err.Description <> "" Then
                    call Print( "Error description: " & Err.Description & ".")
                End If
                Err.Clear
                Exit Sub
            End If
            If objInstance is Nothing Then
                Exit Sub
            Else
                Set objEnumerator = _
                    objInstance.References_("Win32_DependentService")

                If Err.Number Then
                    call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                        " occurred in getting " & _
                        "reference set.")
                    If Err.Description <> "" Then
                        call Print( "Error description: " & _
                            Err.Description & ".")
                    End If
                    Err.Clear
                    Exit Sub
                End If
			
                If objEnumerator.Count = 0 then
                    WScript.Echo "No dependents listed"
		Else
                    i = 0
                    For Each objReference in objEnumerator
                        If objInstance is nothing Then
                            Exit Sub
                        Else
                            ReDim Preserve strName(i)
                            ReDim strDisplay(i), strState(i), intOrder(i)
                            strName(i) = _
                                objService.Get(objReference.Dependent).Name
                            strDisplay(i) = _
                                objService.Get _
                                    (objReference.Dependent).DisplayName
                            strState(i) = _
                                objService.Get(objReference.Dependent).State
                            intOrder(i) = i
                            i = i + 1
                        End If
                        If Err.Number Then
                            Err.Clear
                        End If
                    Next

                   'Display the header
                    strMessage = Space(2) & strPackString("NAME", 20, 1, 1)
                    strMessage = strMessage & strPackString("STATE", 10, 1, 1)
                    strMessage = strMessage & strPackString _
                        ("DISPLAY NAME", 15, 1, 0) & vbCRLF
                    WriteLine strMessage, objOutputFile
                    Call SortArray(strName, True, intOrder, 0)
                    Call ReArrangeArray(strDisplayName, intOrder)
                    Call ReArrangeArray(strState, intOrder)
                    For i = 0 To UBound(strName)
                        strMessage = Space(2) & _
                            strPackString(strName(i), 20, 1, 1)
                        strMessage = strMessage & _
                            strPackString(strState(i), 10, 1, 1)
                        strMessage = strMessage & _
                            strPackString(strDisplay(i), 15, 1, 0)
                        WriteLine strMessage, objOutputFile
                    Next

                End If
            End If

        Case CONST_LIST
            Set objEnumerator = objService.ExecQuery ( _
                "Select Name,DisplayName,State From Win32_Service")
            If Err.Number Then
                call Print( "Error 0x" & CStr(Hex(Err.Number)) & _
                    " occurred during the query.")
                If Err.Description <> "" Then
                    call Print( "Error description: " & Err.Description & ".")
                End If
                Err.Clear
                Exit Sub
            End If
            i = 0
            For Each objInstance in objEnumerator
                If objInstance is nothing Then
                    Exit Sub
                Else
                    ReDim Preserve strName(i), strDisplay(i)
                    ReDim Preserve strState(i), intOrder(i)
                    strName(i) = objInstance.Name
                    strDisplay(i) = objInstance.DisplayName
                    strState(i) = objInstance.State
                    intOrder(i) = i
                    i = i + 1
                End If
                If Err.Number Then
                    Err.Clear
                End If
            Next

            If i > 0 Then
                'Display the header
                strMessage = Space(2) & strPackString("NAME", 20, 1, 1)
                strMessage = strMessage & strPackString("STATE", 10, 1, 1)
                strMessage = strMessage & strPackString("DISPLAY NAME", 15, 1, 0) & vbCRLF
                WriteLine strMessage, objOutputFile
                Call SortArray(strName, True, intOrder, 0)
                Call ReArrangeArray(strDisplayName, intOrder)
                Call ReArrangeArray(strState, intOrder)
                For i = 0 To UBound(strName)
                    strMessage = Space(2) & strPackString(strName(i), 20, 1, 1)
                    strMessage = strMessage & _
                        strPackString(strState(i), 10, 1, 1)
                    strMessage = strMessage & _
                        Left(strPackString(strDisplay(i), 15, 1, 0),47)
                    WriteLine strMessage, objOutputFile
                Next
            Else
                Wscript.Echo "Service not found!"
            End If

    End Select

End Sub

'********************************************************************
'*
'* Sub ShowUsage()
'*
'* Purpose: Shows the correct usage to the user.
'*
'* Input:   None
'*
'* Output:  Help messages are displayed on screen.
'*
'********************************************************************
Private Sub ShowUsage()

    Wscript.Echo ""
    Wscript.Echo "Controls services on a machine."
    Wscript.Echo ""
    Wscript.Echo "SYNTAX:"
    Wscript.Echo "1.  Service.vbs /L"
    Wscript.Echo "               [/S <server>][/U <username>][/W <password>]"
    Wscript.Echo "               [/O <outputfile>]"
    Wscript.Echo ""
    Wscript.Echo "2.  Service.vbs /G | /X | /R | /D | /M <StartMode>"
    Wscript.Echo "                /N <service>"
    Wscript.Echo "               [/S <server>][/U <username>][/W <password>]"
    Wscript.Echo "               [/O <outputfile>]"
    Wscript.Echo ""
    Wscript.Echo "3.  Service.vbs /I /E <execname> /N <service> " _
               & "[/C <DisplayName>]"
    Wscript.Echo "               [/S <server>][/U <username>][/W <password>]"
    Wscript.Echo "               [/O <outputfile>]"
    Wscript.Echo ""
    Wscript.Echo "PARAMETER SPECIFIERS:"
    Wscript.Echo "   /L            List all Services"
    Wscript.Echo "   /D            List Service dependencies"
    Wscript.Echo "   /G            Start a Service"
    Wscript.Echo "   /X            Stop a Service"
    Wscript.Echo "   /R            Remove a Service"
    Wscript.Echo "   /M            Set the Service Mode"
    Wscript.Echo "   /I            Install a service"
    Wscript.Echo "   /E            The Full path and filename of the service"
    Wscript.Echo "   StartMode     The Service Startup Setting."
    Wscript.Echo "   Service       A Service Name"
    Wscript.Echo "   DisplayName   The Service name that appears in the" _
               & " listing/"
    Wscript.Echo "   server        A machine name."
    Wscript.Echo "   username      The current user's name."
    Wscript.Echo "   password      Password of the current user."
    Wscript.Echo "   outputfile    The output file name."
    Wscript.Echo ""
    Wscript.Echo "EXAMPLE:"
    Wscript.Echo "1. cscript Service.vbs /L /S MyMachine2"
    Wscript.Echo "   List installed services for the machine MyMachine2."
    Wscript.Echo "2. cscript Service.vbs /X /N snmp"
    Wscript.Echo "   Stops the snmp service on the current machine."
    Wscript.Echo ""

End Sub

'********************************************************************
'* General Routines
'********************************************************************

'********************************************************************
'*
'* Sub SortArray()
'* Purpose: Sorts an array and arrange another array accordingly.
'* Input:   strArray    the array to be sorted
'*          blnOrder    True for ascending and False for descending
'*          strArray2   an array that has exactly the same number of 
'*                      elements as strArray and will be reordered 
'*                      together with strArray
'*          blnCase     indicates whether the order is case sensitive
'* Output:  The sorted arrays are returned in the original arrays.
'* Note:    Repeating elements are not deleted.
'*
'********************************************************************
Private Sub SortArray(strArray, blnOrder, strArray2, blnCase)

    ON ERROR RESUME NEXT

    Dim i, j, intUbound

    If IsArray(strArray) Then
        intUbound = UBound(strArray)
    Else
        call Print( "Argument is not an array!")
        Exit Sub
    End If

    blnOrder = CBool(blnOrder)
    blnCase = CBool(blnCase)
    If Err.Number Then
        call Print( "Argument is not a boolean!")
        Exit Sub
    End If

    i = 0
    Do Until i > intUbound-1
        j = i + 1
        Do Until j > intUbound
            If blnCase Then     'Case sensitive
                If (strArray(i) > strArray(j)) and blnOrder Then
                    Swap strArray(i), strArray(j)   'swaps element i and j
                    Swap strArray2(i), strArray2(j)
                ElseIf (strArray(i) < strArray(j)) and Not blnOrder Then
                    Swap strArray(i), strArray(j)   'swaps element i and j
                    Swap strArray2(i), strArray2(j)
                ElseIf strArray(i) = strArray(j) Then
                    'Move element j to next to i
                    If j > i + 1 Then
                        Swap strArray(i+1), strArray(j)
                        Swap strArray2(i+1), strArray2(j)
                    End If
                End If
            Else                 'Not case sensitive
                If (LCase(strArray(i)) > LCase(strArray(j))) and blnOrder Then
                    Swap strArray(i), strArray(j)   'swaps element i and j
                    Swap strArray2(i), strArray2(j)
                ElseIf (LCase(strArray(i)) < LCase(strArray(j))) _
                        and Not blnOrder Then
                    Swap strArray(i), strArray(j)   'swaps element i and j
                    Swap strArray2(i), strArray2(j)
                ElseIf LCase(strArray(i)) = LCase(strArray(j)) Then
                    'Move element j to next to i
                    If j > i + 1 Then
                        Swap strArray(i+1), strArray(j)
                        Swap strArray2(i+1), strArray2(j)
                    End If
                End If
            End If
            j = j + 1
        Loop
        i = i + 1
    Loop

End Sub

'********************************************************************
'*
'* Sub Swap()
'* Purpose: Exchanges values of two strings.
'* Input:   strA    a string
'*          strB    another string
'* Output:  Values of strA and strB are exchanged.
'*
'********************************************************************
Private Sub Swap(ByRef strA, ByRef strB)

    Dim strTemp

    strTemp = strA
    strA = strB
    strB = strTemp

End Sub

'********************************************************************
'*
'* Sub ReArrangeArray()
'* Purpose: Rearranges one array according to order specified in another array.
'* Input:   strArray    the array to be rearranged
'*          intOrder    an integer array that specifies the order
'* Output:  strArray is returned as rearranged
'*
'********************************************************************
Private Sub ReArrangeArray(strArray, intOrder)

    ON ERROR RESUME NEXT

    Dim intUBound, i, strTempArray()

    If Not (IsArray(strArray) and IsArray(intOrder)) Then
        call Print( "At least one of the arguments is not an array")
        Exit Sub
    End If

    intUBound = UBound(strArray)

    If intUBound <> UBound(intOrder) Then
        call Print( "The upper bound of these two arrays do not match!")
        Exit Sub
    End If

    ReDim strTempArray(intUBound)

    For i = 0 To intUBound
        strTempArray(i) = strArray(intOrder(i))
        If Err.Number Then
            call Print( "Error 0x" & CStr(Hex(Err.Number)) & " occurred in " _
                      & "rearranging an array.")
            If Err.Description <> "" Then
                call Print( "Error description: " & Err.Description & ".")
            End If
            Err.Clear
            Exit Sub
        End If
    Next

    For i = 0 To intUBound
        strArray(i) = strTempArray(i)
    Next

End Sub

'********************************************************************
'*
'* Function strPackString()
'*
'* Purpose: Attaches spaces to a string to increase the length to intWidth.
'*
'* Input:   strString   a string
'*          intWidth    the intended length of the string
'*          blnAfter    Should spaces be added after the string?
'*          blnTruncate specifies whether to truncate the string or not if
'*                      the string length is longer than intWidth
'*
'* Output:  strPackString is returned as the packed string.
'*
'********************************************************************
Private Function strPackString( ByVal strString, _
                                ByVal intWidth,  _
                                ByVal blnAfter,  _
                                ByVal blnTruncate)

    ON ERROR RESUME NEXT

    intWidth      = CInt(intWidth)
    blnAfter      = CBool(blnAfter)
    blnTruncate   = CBool(blnTruncate)

    If Err.Number Then
        Call Wscript.Echo ("Argument type is incorrect!")
        Err.Clear
        Wscript.Quit
    End If

    If IsNull(strString) Then
        strPackString = "null" & Space(intWidth-4)
        Exit Function
    End If

    strString = CStr(strString)
    If Err.Number Then
        Call Wscript.Echo ("Argument type is incorrect!")
        Err.Clear
        Wscript.Quit
    End If

    If intWidth > Len(strString) Then
        If blnAfter Then
            strPackString = strString & Space(intWidth-Len(strString))
        Else
            strPackString = Space(intWidth-Len(strString)) & strString & " "
        End If
    Else
        If blnTruncate Then
            strPackString = Left(strString, intWidth-1) & " "
        Else
            strPackString = strString & " "
        End If
    End If

End Function

'********************************************************************
'* 
'*  Function blnGetArg()
'*
'*  Purpose: Helper to intParseCmdLine()
'* 
'*  Usage:
'*
'*     Case "/s" 
'*       blnGetArg ("server name", strServer, intArgIter)
'*
'********************************************************************
Private Function blnGetArg ( ByVal StrVarName,   _
                             ByRef strVar,       _
                             ByRef intArgIter) 

    blnGetArg = False 'failure, changed to True upon successful completion

    If Len(Wscript.Arguments(intArgIter)) > 2 then
        If Mid(Wscript.Arguments(intArgIter),3,1) = ":" then
            If Len(Wscript.Arguments(intArgIter)) > 3 then
                strVar = Right(Wscript.Arguments(intArgIter), _
                         Len(Wscript.Arguments(intArgIter)) - 3)
                blnGetArg = True
                Exit Function
            Else
                intArgIter = intArgIter + 1
                If intArgIter > (Wscript.Arguments.Count - 1) Then
                    Call Wscript.Echo( "Invalid " & StrVarName & ".")
                    Call Wscript.Echo( "Please check the input and try again.")
                    Exit Function
                End If

                strVar = Wscript.Arguments.Item(intArgIter)
                If Err.Number Then
                    Call Wscript.Echo( "Invalid " & StrVarName & ".")
                    Call Wscript.Echo( "Please check the input and try again.")
                    Exit Function
                End If

                If InStr(strVar, "/") Then
                    Call Wscript.Echo( "Invalid " & StrVarName)
                    Call Wscript.Echo( "Please check the input and try again.")
                    Exit Function
                End If

                blnGetArg = True 'success
            End If
        Else
            strVar = Right(Wscript.Arguments(intArgIter), _
                     Len(Wscript.Arguments(intArgIter)) - 2)
            blnGetArg = True 'success
            Exit Function
        End If
    Else
        intArgIter = intArgIter + 1
        If intArgIter > (Wscript.Arguments.Count - 1) Then
            Call Wscript.Echo( "Invalid " & StrVarName & ".")
            Call Wscript.Echo( "Please check the input and try again.")
            Exit Function
        End If

        strVar = Wscript.Arguments.Item(intArgIter)
        If Err.Number Then
            Call Wscript.Echo( "Invalid " & StrVarName & ".")
            Call Wscript.Echo( "Please check the input and try again.")
            Exit Function
        End If

        If InStr(strVar, "/") Then
            Call Wscript.Echo( "Invalid " & StrVarName)
            Call Wscript.Echo( "Please check the input and try again.")
            Exit Function
        End If
        blnGetArg = True 'success
    End If
End Function

'********************************************************************
'*
'* Function blnConnect()
'*
'* Purpose: Connects to machine strServer.
'*
'* Input:   strServer       a machine name
'*          strNameSpace    a namespace
'*          strUserName     name of the current user
'*          strPassword     password of the current user
'*
'* Output:  objService is returned  as a service object.
'*          strServer is set to local host if left unspecified
'*
'********************************************************************
Private Function blnConnect(ByVal strNameSpace, _
                            ByVal strUserName,  _
                            ByVal strPassword,  _
                            ByRef strServer,    _
                            ByRef objService)

    ON ERROR RESUME NEXT

    Dim objLocator, objWshNet

    blnConnect = False     'There is no error.

    'Create Locator object to connect to remote CIM object manager
    Set objLocator = CreateObject("WbemScripting.SWbemLocator")
    If Err.Number then
        Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & _
                           " occurred in creating a locator object." )
        If Err.Description <> "" Then
            Call Wscript.Echo( "Error description: " & Err.Description & "." )
        End If
        Err.Clear
        blnConnect = True     'An error occurred
        Exit Function
    End If

    'Connect to the namespace which is either local or remote
    Set objService = objLocator.ConnectServer (strServer, strNameSpace, _
       strUserName, strPassword)
    ObjService.Security_.impersonationlevel = 3
    If Err.Number then
        Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & _
                           " occurred in connecting to server " _
           & strServer & ".")
        If Err.Description <> "" Then
            Call Wscript.Echo( "Error description: " & Err.Description & "." )
        End If
        Err.Clear
        blnConnect = True     'An error occurred
    End If

    'Get the current server's name if left unspecified
    If IsEmpty(strServer) Then
        Set objWshNet = CreateObject("Wscript.Network")
    strServer     = objWshNet.ComputerName
    End If

End Function

'********************************************************************
'*
'* Sub      VerifyHostIsCscript()
'*
'* Purpose: Determines which program is used to run this script.
'*
'* Input:   None
'*
'* Output:  If host is not cscript, then an error message is printed 
'*          and the script is aborted.
'*
'********************************************************************
Sub VerifyHostIsCscript()

    ON ERROR RESUME NEXT

    Dim strFullName, strCommand, i, j, intStatus

    strFullName = WScript.FullName

    If Err.Number then
        Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & " occurred." )
        If Err.Description <> "" Then
            Call Wscript.Echo( "Error description: " & Err.Description & "." )
        End If
        intStatus =  CONST_ERROR
    End If

    i = InStr(1, strFullName, ".exe", 1)
    If i = 0 Then
        intStatus =  CONST_ERROR
    Else
        j = InStrRev(strFullName, "\", i, 1)
        If j = 0 Then
            intStatus =  CONST_ERROR
        Else
            strCommand = Mid(strFullName, j+1, i-j-1)
            Select Case LCase(strCommand)
                Case "cscript"
                    intStatus = CONST_CSCRIPT
                Case "wscript"
                    intStatus = CONST_WSCRIPT
                Case Else       'should never happen
                    Call Wscript.Echo( "An unexpected program was used to " _
                                       & "run this script." )
                    Call Wscript.Echo( "Only CScript.Exe or WScript.Exe can " _
                                       & "be used to run this script." )
                    intStatus = CONST_ERROR
                End Select
        End If
    End If

    If intStatus <> CONST_CSCRIPT Then
        Call WScript.Echo( "Please run this script using CScript." & vbCRLF & _
             "This can be achieved by" & vbCRLF & _
             "1. Using ""CScript Service.vbs arguments"" for Windows 95/98 or" _
             & vbCRLF & "2. Changing the default Windows Scripting Host " _
             & "setting to CScript" & vbCRLF & "    using ""CScript " _
             & "//H:CScript //S"" and running the script using" & vbCRLF & _
             "    ""Service.vbs arguments"" for Windows NT/2000." )
        WScript.Quit
    End If

End Sub

'********************************************************************
'*
'* Sub WriteLine()
'* Purpose: Writes a text line either to a file or on screen.
'* Input:   strMessage  the string to print
'*          objFile     an output file object
'* Output:  strMessage is either displayed on screen or written to a file.
'*
'********************************************************************
Sub WriteLine(ByVal strMessage, ByVal objFile)

    On Error Resume Next
    If IsObject(objFile) then        'objFile should be a file object
        objFile.WriteLine strMessage
    Else
        Call Wscript.Echo( strMessage )
    End If

End Sub

'********************************************************************
'* 
'* Function blnErrorOccurred()
'*
'* Purpose: Reports error with a string saying what the error occurred in.
'*
'* Input:   strIn		string saying what the error occurred in.
'*
'* Output:  displayed on screen 
'* 
'********************************************************************
Private Function blnErrorOccurred (ByVal strIn)

    If Err.Number Then
        Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & ": " & strIn)
        If Err.Description <> "" Then
            Call Wscript.Echo( "Error description: " & Err.Description)
        End If
        Err.Clear
        blnErrorOccurred = True
    Else
        blnErrorOccurred = False
    End If

End Function

'********************************************************************
'* 
'* Function blnOpenFile
'*
'* Purpose: Opens a file.
'*
'* Input:   strFileName		A string with the name of the file.
'*
'* Output:  Sets objOpenFile to a FileSystemObject and setis it to 
'*            Nothing upon Failure.
'* 
'********************************************************************
Private Function blnOpenFile(ByVal strFileName, ByRef objOpenFile)

    ON ERROR RESUME NEXT

    Dim objFileSystem

    Set objFileSystem = Nothing

    If IsEmpty(strFileName) OR strFileName = "" Then
        blnOpenFile = False
        Set objOpenFile = Nothing
        Exit Function
    End If

    'Create a file object
    Set objFileSystem = CreateObject("Scripting.FileSystemObject")
    If blnErrorOccurred("Could not create filesystem object.") Then
        blnOpenFile = False
        Set objOpenFile = Nothing
        Exit Function
    End If

    'Open the file for output
    Set objOpenFile = objFileSystem.OpenTextFile(strFileName, 8, True)
    If blnErrorOccurred("Could not open") Then
        blnOpenFile = False
        Set objOpenFile = Nothing
        Exit Function
    End If
    blnOpenFile = True

End Function

Private Function WaitStatus (objService, strServiceName, strStatus, WaitTime)
	Dim Elapsed, objInstance
	
	Wscript.Echo ( "Waiting for Service " & strServiceName & " to enter a " & strStatus & " state.")
	Wscript.Echo ( "Waiting up to " & WaitTime & " seconds for the final status")
	WaitStatus = vbFalse
	WaitTime = WaitTime * 1000
	Elapsed = 0
	Do While Elapsed < WaitTime
		Wscript.Sleep (100)
		Elapsed = Elapsed + 100
		Set objInstance = objService.Get("Win32_Service='" &_
	        	strServiceName & "'")
		If Err.Number Then
	        	WScript.Echo ( "Error 0x" & CStr(Hex(Err.Number)) & _
				" occurred in getting " & _
				"service " & strServiceName & ".")
			If Err.Description <> "" Then
				call WScript.Echo ( "Error description: " & Err.Description & ".")
			End If
			Err.Clear
	                Exit Function
		End If
		If objInstance is nothing Then
			Exit Function
		Else
			If objInstance.State = strStatus and _
			   objInstance.Status = "OK" Then 
			   	WaitStatus = vbTrue
			   	Exit Function
			End If
			If strStatus = "Stopped" and _
			   (objInstance.State <> "Stopped" and _
			    objInstance.State <> "Stop Pending") and _
			   (objInstance.Status <> "OK" and _
			    objInstance.Status <> "Stop Pending") Then
			    	Exit Function
			End If
			If strStatus = "Running" and _
			   (objInstance.State <> "Running" and _
			    objInstance.State <> "Start Pending") and _
			   (objInstance.Status <> "OK" and _
			    objInstance.Status <> "Start Pending") Then 
				Exit Function
			End If
		End If
	loop
End Function 

'********************************************************************
'*
'* Function intParseCmdLine()
'*
'* Purpose: Parses the command line.
'* Input:   
'*
'* Output:  strServer          a remote server ("" = local server")
'*          strUserName        the current user's name
'*          strPassword        the current user's password
'*          strOutputFile      an output file name
'*          strTaskCommand     one of /list, /start, /stop /install /remove
'*                                    /dependents
'*          strDriverName      name of the Service
'*          strStartMode       start mode of the Service
'*          strDisplayName     Display name for the Service
'*          strExecName        The Full path of the executable

'*
'********************************************************************
Private Function intParseCmdLine (ByRef strServer      ,  _
                                  ByRef strUserName    ,  _
                                  ByRef strPassword    ,  _
                                  ByRef strOutputFile  ,  _
                                  ByRef strTaskCommand ,  _
                                  ByRef strServiceName ,  _
                                  ByRef strStartMode   ,  _
                                  ByRef strDisplayName ,  _
                                  ByRef strExecName    ,  _
				  ByRef strDepends     )

    Dim oArgs, key, strKey

    set oArgs = ParseArgs
    If oArgs.count = 0 Then                'No arguments have been received
        intParseCmdLine = CONST_PROCEED
        strTaskCommand = CONST_LIST
        Exit Function
    End If

    'Check if the user is asking for help or is just confused
    If  oArgs.Exists("/?")    OR oArgs.Exists("-?") or _
	oArgs.Exists("/h")    OR oArgs.Exists("-h") or _
	oArgs.Exists("/help") OR oArgs.Exists("-help") Then
        intParseCmdLine = CONST_SHOW_USAGE
        Exit Function
    End If

    'Retrieve the command line and set appropriate variables
    for each key in oArgs.Keys
        Select Case trim(lCase(key))
            Case "/s", "/server"
		strServer = oArgs(key)
                If strServer = "" Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If

            Case "/o", "/output"
		strOutputFile = oArgs(key)
                If strOutputFile = "" Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If

            Case "/u", "/user"
		strUserName = oArgs(key)
                If strUserName = "" Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If

            Case "/w", "/password"
		strPassword = oArgs(key)
                If strPassword = "" Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If

            Case "/l", "/list"
                intParseCmdLine = CONST_PROCEED
                strTaskCommand = CONST_LIST
               
            Case "/g", "/start"
                intParseCmdLine = CONST_PROCEED
                strTaskCommand = CONST_START
              
            Case "/x", "/stop"
                intParseCmdLine = CONST_PROCEED
                strTaskCommand = CONST_STOP

            Case "/r", "/remove"
                intParseCmdLine = CONST_PROCEED
                strTaskCommand = CONST_REMOVE

            Case "/i", "/install"
                intParseCmdLine = CONST_PROCEED
                strTaskCommand = CONST_INSTALL

            Case "/d", "/depends"
		strDepends = oArgs(key)
		bIncludeDepends = true
		if isempty(strTaskCommand) then 
			intParseCmdLine = CONST_PROCEED
	                strTaskCommand = CONST_DEPENDS
		end if
            Case "/m", "/mode"
                if IsEmpty(strTaskCommand) then
			intParseCmdLine = CONST_PROCEED
	                strTaskCommand = CONST_MODE
		end if
		strStartMode = oArgs(key)
                If strStartMode = "" Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If
            
            Case "/c", "/displayname"
		strDisplayName = oArgs(key)
                If strDisplayName = "" then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If

            Case "/n", "/name"
		strServiceName = oArgs(key)
                If strServiceName = "" Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If

            Case "/e", "/exe"
		strExecName = oArgs(key)
                If strExecName = "" Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If

            Case Else 'We shouldn't get here
                Wscript.Echo("Invalid or misplaced parameter: " _
                   & oArgs(key) & vbCRLF _
                   & "Please check the input and try again," & vbCRLF _
                   & "or invoke with '/?' for help with the syntax.")
                Wscript.Quit

        End Select
    Next

    If IsEmpty(strTaskCommand) Then 
        intParseCmdLine = CONST_PROCEED
        strTaskCommand = CONST_LIST
        Exit Function
    End If

    Select Case strTaskCommand
        Case CONST_START
            If IsEmpty(strServiceName) then
                intParseCmdLine = CONST_ERROR
            End IF
        Case CONST_STOP
            If IsEmpty(strServiceName) then
                intParseCmdLine = CONST_ERROR
            End IF
        Case CONST_DEPENDS
            If IsEmpty(strServiceName) then
                intParseCmdLine = CONST_ERROR
            End IF
        Case CONST_REMOVE
            If IsEmpty(strServiceName) then
                intParseCmdLine = CONST_ERROR
            End IF
        Case CONST_MODE
            If IsEmpty(strServiceName) then
                intParseCmdLine = CONST_ERROR
            End IF
        Case CONST_INSTALL
            If IsEmpty(strServiceName) then
                intParseCmdLine = CONST_ERROR
            End If
            If IsEmpty(strExecName) then
                intParseCmdLine = CONST_ERROR
            End IF
    End Select

End Function

'*************************************************************************
'* Create a Dictionary Object to hold our command line arguments         *
'*************************************************************************
function ParseArgs()
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
	       pair(1) = replace (pair(1), chr(39), chr(34))
               g_oSwitches.add Trim(pair(0)), Trim(pair(1))
          Else
               g_oSwitches.add Trim(pair(0)),""
          End If
     Next
     Set ParseArgs = g_oSwitches
End function

Public function DisplayStatus (strAction, sName, intStatus) 
	Dim strStatus 
	strStatus = strAction & " method for service " & sName 
	Select Case intStatus
		Case 0  strStatus = strStatus & " Succeeded"
		Case 1	strStatus = strStatus & " Not Supported"
		Case 2	strStatus = strStatus & " Access Denied"
		Case 3	strStatus = strStatus & " Dependent Services Running"
		Case 4	strStatus = strStatus & " Invalid Service Control"
		Case 5	strStatus = strStatus & " Service Cannot Accept Control"
		Case 6	strStatus = strStatus & " Service Not Active"
		Case 7	strStatus = strStatus & " Service Request Timeout"
		Case 8	strStatus = strStatus & " Unknown Failure"
		Case 9	strStatus = strStatus & " Path Not Found"
		Case 10	strStatus = strStatus & " Service Already Running"
		Case 11	strStatus = strStatus & " Service Database Locked"
		Case 12	strStatus = strStatus & " Service Dependency Deleted"
		Case 13	strStatus = strStatus & " Service Dependency Failure"
		Case 14	strStatus = strStatus & " Service Disabled"
		Case 15	strStatus = strStatus & " Service Logon Failure"
		Case 16	strStatus = strStatus & " Service Marked For Deletion"
		Case 17	strStatus = strStatus & " Service No Thread"
		Case 18	strStatus = strStatus & " Status Circular Dependency"
		Case 19	strStatus = strStatus & " Status Duplicate Name"
		Case 20	strStatus = strStatus & " Status Invalid Name"
		Case 21	strStatus = strStatus & " Status Invalid Parameter"
		Case 22	strStatus = strStatus & " Status Invalid Service Account"
		Case 23	strStatus = strStatus & " Status Service Exists"
		Case 24	strStatus = strStatus & " Service Paused"
		Case else strStatus = strStatus & " Unknown " & intStatus
	End Select
	strStatus = strStatus
	DisplayStatus = strStatus
end function

'********************************************************************
'*                                                                  *
'*                           End of File                            *
'*                                                                  *
'********************************************************************

