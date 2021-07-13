'********************************************************************
'*
'* File:           DriveSpace.vbs
'* Created:        March 2005
'*
'* Main Function:
'*
'*
'* Portions of this script have been borrowed from the MSDN website
'* and customized to meet Covansys needs.
'*
'* Copyright (C) 2005 Covansys
'* Copyright (C) 2005 Microsoft Corporation
'*
'********************************************************************
Option Explicit

'Define constants
Const CONST_ERROR                   = 0
Const CONST_WSCRIPT                 = "Wscript"
Const CONST_CSCRIPT                 = "Cscript"
Const CONST_SHOW_USAGE              = 3
Const CONST_PROCEED                 = 4
Const CONST_ERRORLEVEL_ON	    	= "On"
Const CONST_ERRORLEVELOFF	    	= "Off"
Const CONST_LIST                    = "List"
Const CONST_CHECK		    		= "Check"					'Default action
Const CONST_BARE_OUTPUT		    	= "bare"
Const CONST_STANDARD_OUTPUT			= "standard"
Const CONST_VERBOSE_OUTPUT	    	= "verbose"
Const CONST_TIMESTAMP_ON			= "On"
Const CONST_TIMESTAMP_OFF			= "Off"
Const CONST_PASSED		    		= "Passed"
Const CONST_FAILED		    		= "Failed"
Const CONST_UNAVAILABLE		    	= "Unavailable"
Const CONST_CONVERSION_FACTOR 	    = 1048576
Const CONST_GROUPDIGITS		    	= -1
Const CONST_NO_GROUP_DIGITS 		= 0
Const CONST_NO_DECIMAL_PLACES 	    = 0
Const CONST_LEADING_ZERO 			= -1
Const CONST_NEGS_PARENTHESIS 		= -1
Const CONST_DEFAULT_THRESHHOLD	    = 200


'Declare variables
Dim intOpMode
Dim strOutputFile, strOutputType, strTimeStamp
Dim strComputer, strUserName, strPassword
Dim strThreshhold, strTaskCommand, strDriveLetters
Dim strErrorLevel

'Make sure the host is cscript, if not then abort
VerifyHostIsCscript()

'Parse the command line

intOpMode = intParseCmdLine( strComputer   	,  _
                             strUserName   	,  _
                             strPassword   	,  _
                             strOutputFile 	,  _
                             strOutputType 	,  _
                             strTimeStamp  	,  _
                             strThreshhold 	,  _
                             strTaskCommand	,  _
                             strDriveLetters,  _
                             strErrorLevel)
                             
Select Case intOpMode

    Case CONST_SHOW_USAGE
        Call ShowUsage()

    Case CONST_PROCEED
        Call CheckDriveSpace(strComputer   	,  _
                             strUserName   	,  _
                             strPassword   	,  _
                             strOutputFile 	,  _
                             strOutputType 	,  _
                             strTimeStamp  	,  _
                             strThreshhold 	,  _
                             strTaskCommand	,  _
                             strDriveLetters,  _
                             strErrorLevel)
				

    Case CONST_ERROR
        Call Wscript.Echo ("Error occurred in passing parameters.")

    Case Else                    'Default -- should never happen
        Call Print("Error occurred in passing parameters.")

End Select
'********************************************************************
'*
'* Sub      CheckDriveSpace()
'*
'* Purpose: Checks available free space for hard disk on the server
'*
'* Input:   strComputer		Computer Name
'*	    	strUserName		User name to connect as
'*	    	strPassword		Password for the user
'*	    	strOutputFile	Output file path and name
'*			strOutputType	Output display type
'*			strTimeStamp	Timestamp option
'*	    	strThreshhold	Theshhold amount for low disk space
'*	    	strTaskCommand	one of /list /check
'*	    	strDriveLetters	Drive letters specified
'*	    	strErrorLevel	Can set error level if set
'*
'* Output:  Displays drive information
'*
'********************************************************************
Private Sub CheckDriveSpace(strComputer		,  _
                            strUserName		,  _
                            strPassword		,  _
                            strOutputFile	,  _
                            strOutputType	,  _
                            strTimeStamp	,  _
                            strThreshhold	,  _
                            strTaskCommand	,  _
                            strDriveLetters	,  _
                            strErrorLevel )
                            
    ON ERROR RESUME NEXT
    
    Dim objFileSystem, objOutputFile, objWMIService

    'Open a text file for output if the file is requested
    If Not IsEmpty(strOutputFile) Then
        If (NOT blnOpenFile(strOutputFile, objOutputFile)) Then
            Call Wscript.Echo ("Could not open an output file.")
            Exit Sub
        End If
    End If
    
    'Establish a connection with the server.
    If NOT blnConnect(strUserName	, _
                  strPassword		, _
                  strOutputType		, _
                  strComputer		, _
                  objWMIService) Then
        Call Wscript.Echo("")
        Call Wscript.Echo("Error occured connecting to WMI for " & strComputer)
        Exit Sub
    End If

    
    Call ExecuteMethod(objWMIService	,_
    				objOutputFile		,_
                	strThreshhold  		,_
                	strDriveLetters 	,_
                	strTaskCommand 		,_
                	strOutputType  		,_
                	strErrorLevel		,_
                	strComputer			,_
                	strUserName			,_
                	strPassword			,_ 
                	strOutputFile		,_
                	strTimeStamp)
    			
End Sub
'********************************************************************
'*
'* Sub      ExecuteMethod()
'*
'* Purpose: Checks available free space for hard disk on the server
'*
'* Input:   objWMIService	WMI Object
'*	    	objOutputFile	Output file
'*			strThreshhold	Comparison value
'*			strDriveLetters	Specified drive letters
'*	    	strTaskCommand	one of /list /check
'*	    	strOutputType	Omits header info if set
'*	    	strErrorLevel	Can set error level if set
'*			strComputer		Computer name
'*			strUserName		User name
'*			strPassword		Password
'*			strOutputFile	Output file path
'*			strTimeStamp	Time Stamp option
'*
'* Output:  Available drive space
'*
'********************************************************************
Sub ExecuteMethod	(objWMIService	,_
    				objOutputFile	,_
                	strThreshhold	,_
                	strDriveLetters	,_
                	strTaskCommand	,_
                	strOutputType	,_
                	strErrorLevel	,_
                	strComputer		,_
                	strUserName		,_
                	strPassword		,_ 
                	strOutputFile	,_
                	strTimeStamp)
                	

	Dim i
	Dim arrNoDrive()																'stores drive letters that dont exist on the pc
	Dim arrValidDriveLetters()														'stores specified drive letters that exist on the pc
																					' and are logical disk
	Dim arrInvalidDriveType()														'stores the 'type' of drive that is not valid
	Dim arrNonLogicalDiskDriveLetters()												'stores the drive letters that are not logical drives
																					' and drives that don't exist on the pc
	Dim arrLogicalDiskDriveLetters()												'stores the drive letter for all logical disk on the pc
	Dim arrInvalidDriveLetters()													'stores only all non logical disk that exist on the pc
	Dim arrDriveFreeSpace()															'stores the freespace for all logical disk on the pc in MB																	'array stored as a delimited string
	Dim arrDriveStatus()															'array that stores comparison results
	
	
	
	Select Case strTaskCommand
		Case CONST_LIST
			Call DriveLetters(strDriveLetters)
			Call GetDriveInformation(objWMIService					,	_
									arrNonLogicalDiskDriveLetters	, 	_
									arrInvalidDriveType				,	_
									arrValidDriveLetters			,	_
									arrInvalidDriveLetters			,	_
									arrNoDrive						,	_
									arrLogicalDiskDriveLetters		,	_
									arrDriveFreeSpace				,	_
									strDriveLetters)
			Call WriteOutput(strOutputType					,_
							strDriveLetters					,_
							strThreshhold					,_
							strTaskCommand					,_
							strTimeStamp					,_
							strOutputFile					,_
							strErrorLevel					,_
							strComputer						,_
							strUserName						,_
							strPassword						,_
							arrDriveFreeSpace				,_
							arrNonLogicalDiskDriveLetters	,_
							arrInvalidDriveType				,_
							arrValidDriveLetters			,_
							arrInvalidDriveLetters			,_
							arrNoDrive						,_
							arrLogicalDiskDriveLetters		,_
							arrDriveStatus					,_
							objOutputFile)
		Case CONST_CHECK
			Call DriveLetters(strDriveLetters)
			Call GetDriveInformation(objWMIService					,	_
									arrNonLogicalDiskDriveLetters	, 	_
									arrInvalidDriveType				,	_
									arrValidDriveLetters			,	_
									arrInvalidDriveLetters			,	_
									arrNoDrive						,	_
									arrLogicalDiskDriveLetters		,	_
									arrDriveFreeSpace				,	_
									strDriveLetters)
			Call Comparison(arrDriveFreeSpace			, _
							arrLogicalDiskDriveLetters	, _
							arrValidDriveLetters		, _
							strThreshhold				, _
							arrDriveStatus)
			Call WriteOutput(strOutputType					,_
							strDriveLetters					,_
							strThreshhold					,_
							strTaskCommand					,_
							strTimeStamp					,_
							strOutputFile					,_
							strErrorLevel					,_
							strComputer						,_
							strUserName						,_
							strPassword						,_
							arrDriveFreeSpace				,_
							arrNonLogicalDiskDriveLetters	,_
							arrInvalidDriveType				,_
							arrValidDriveLetters			,_
							arrInvalidDriveLetters			,_
							arrNoDrive						,_
							arrLogicalDiskDriveLetters		,_
							arrDriveStatus					,_
							objOutputFile)
	End Select
	If (strErrorLevel = CONST_ERRORLEVEL_ON) AND (strTaskCommand = CONST_CHECK) Then
		For i = 0 to UBound(arrDriveStatus)
			If arrDriveStatus(i) = CONST_FAILED Then
				WScript.Quit(1)
			End If
		Next
	End If

End Sub
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
             "1. Using ""CScript DriveSpace.vbs arguments"" for Windows 95/98 or" _
             & vbCRLF & "2. Changing the default Windows Scripting Host " _
             & "setting to CScript" & vbCRLF & "    using ""CScript " _
             & "//H:CScript //S"" and running the script using" & vbCRLF & _
             "    ""DriveSpace.vbs arguments"" for Windows NT/2000." )
        WScript.Quit
    End If

End Sub
'********************************************************************
'*
'* Function intParseCmdLine()
'*
'* Purpose: Parses the command line.
'* Input:   
'*
'* Output:  strComputer         a remote computer ("" = local computer")
'*          strUserName         the current user's name
'*          strPassword         the current user's password
'*          strOutputFile       an output file name
'*			strOutputType		output display type
'*			strTimeStamp		timestamp option
'*          strThreshhold       Comparison value
'*			strTaskCommand		one of /list /check
'*	    	strDriveLetters		specified driveletters
'*	   		strErrorLevel		option to set errorlevel
'*
'********************************************************************
Private Function intParseCmdLine( ByRef strComputer   	,  _
                             	ByRef strUserName   	,  _
                             	ByRef strPassword   	,  _
                             	ByRef strOutputFile 	,  _
                             	ByRef strOutputType 	,  _
                             	ByRef strTimeStamp  	,  _
                         		ByRef strThreshhold 	,  _
                             	ByRef strTaskCommand	,  _
                            	ByRef strDriveLetters	,  _
                             	ByRef strErrorLevel)

    ON ERROR RESUME NEXT

    Dim strFlag
    Dim intState, intArgIter
    Dim objFileSystem

    If Wscript.Arguments.Count > 0 Then
        strFlag = Wscript.arguments.Item(0)
    End If

    If IsEmpty(strFlag) Then                'No arguments have been received
        intParseCmdLine = CONST_PROCEED
        strTaskCommand = CONST_CHECK
        strErrorLevel = CONST_ERRORLEVEL_ON
        strTimeStamp = CONST_TIMESTAMP_OFF
        strOutputType = CONST_STANDARD_OUTPUT
        Exit Function
    End If

    'Check if the user is asking for help or is just confused
    If (strFlag="help") OR (strFlag="/h") OR (strFlag="\h") OR (strFlag="-h") _
        OR (strFlag = "\?") OR (strFlag = "/?") OR (strFlag = "?") _ 
        OR (strFlag="h") Then
        intParseCmdLine = CONST_SHOW_USAGE
        Exit Function
    End If

    'Retrieve the command line and set appropriate variables
     intArgIter = 0
    Do While intArgIter <= Wscript.arguments.Count - 1
        Select Case Left(LCase(Wscript.arguments.Item(intArgIter)),2)
  
            
            Case "/c"
                If Not blnGetArg("Computer", strComputer, intArgIter) Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If
                intArgIter = intArgIter + 1
                
            Case "/d"
            	If Not blnGetArg("Drive Letters", strDriveLetters, intArgIter) Then
            		intParseCmdLine = CONST_ERROR
            		Exit Function
            	End If
            	intArgIter = intArgIter + 1
            	
            Case "/e"
            	intParseCmdLine = CONST_PROCEED
				strErrorLevel = CONST_ERRORLEVEL_OFF
                intArgIter = intArgIter + 1
                
            Case "/l"
            	intParseCmdLine = CONST_PROCEED            
                strTaskCommand = CONST_LIST
                intArgIter = intArgIter + 1

            Case "/o"
                If Not blnGetArg("Output File", strOutputFile, intArgIter) Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If
                intArgIter = intArgIter + 1
                
            Case "/q"
            	If Not blnGetArg("Output Type", strOutputType, intArgIter) Then
            		intParseCmdLine = CONST_ERROR
            		Exit Function
            	End If
                intArgIter = intArgIter + 1
            
            Case "/s"
            	intParseCmdLine = CONST_PROCEED
            	strTimeStamp = CONST_TIMESTAMP_ON
            	intArgIter = intArgIter + 1
                
            Case "/t"
                If Not blnGetArg("Threshhold Amount", strThreshhold, intArgIter) Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If
                intArgIter = intArgIter + 1

            Case "/u"
                If Not blnGetArg("User Name", strUserName, intArgIter) Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If
                intArgIter = intArgIter + 1
                
            Case "/w"
                If Not blnGetArg("User Password", strPassword, intArgIter) Then
                    intParseCmdLine = CONST_ERROR
                    Exit Function
                End If
                intArgIter = intArgIter + 1      

            Case Else 'We shouldn't get here
                Call Wscript.Echo("Invalid or misplaced parameter: " _
                   & Wscript.arguments.Item(intArgIter) & vbCRLF _
                   & "Please check the input and try again," & vbCRLF _
                   & "or invoke with '/?' for help with the syntax.")
                Wscript.Quit

        End Select

    Loop '** intArgIter <= Wscript.arguments.Count - 1
	
	'Makes sure the strOutputType argument is acceptable
	If NOT IsEmpty(strOutputType) Then
		strOutputType = LCase(strOutputType)
		If NOT strOutputType = CONST_BARE_OUTPUT Then
			If NOT strOutputType = CONST_STANDARD_OUTPUT Then
				If NOT strOutputType = CONST_VERBOSE_OUTPUT Then
					IntParseCmdLine = CONST_ERROR
					Exit Function
				End If
			End If
		End If
	Else
		strOutputType = CONST_STANDARD_OUTPUT
	End If	
	
	If IsEmpty(strErrorLevel) Then
		strErrorLevel = CONST_ERRORLEVEL_ON
	End If
	
	If IsEmpty(strTimeStamp) Then
		strTimeStamp = CONST_TIMESTAMP_OFF
	End If
			
    If IsEmpty(strTaskCommand) Then 
        intParseCmdLine = CONST_PROCEED
        strTaskCommand = CONST_CHECK
        Exit Function
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
'*       blnGetArg ("server name", strComputer, intArgIter)
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
'* Purpose: Connects to machine strComputer.
'*
'* Input:   strUserName     name of the current user
'*          strPassword     password of the current user
'*          strComputer     computer name
'*          objWMIService   WMI Object
'*
'* Output:  objWMIService	is returned  as a service object.
'*          strComputer 	is set to local host if left unspecified
'*
'********************************************************************
Private Function blnConnect(ByVal strUserName	,_
                            ByVal strPassword	,_
                            ByVal strOutputType	,_
                            ByRef strComputer	,_
                            ByRef objWMIService)
                            
    ON ERROR RESUME NEXT
    
    Dim strLocalComputer
    
    blnConnect = True

    Call getLocalComputerName(strLocalComputer)
    If IsEmpty(strComputer) Then
    	strComputer = strLocalComputer
    End If
	
	'Test to see if a remote connection is needed
    If NOT UCase(strLocalComputer) = UCase(strComputer) Then
		If NOT getRemoteConnect("root\cimv2" , _
	                       		strUserName , _
	                       		strPassword , _
	                       		strComputer , _
	                       		objWMIService ) Then
			Call Wscript.Echo("")
			Call Wscript.Echo("Please check the computer name, credentials and WBEM Core.")
			blnConnect = False
			Exit Function
	    End If
	'Else a local connection is needed
    Else 
    	If NOT (IsEmpty(strUserName) AND IsEmpty(strPassword)) Then
    		If NOT strOutputType = CONST_BARE_OUTPUT Then
	    	    'Note stating limitations of connecting to WMI locally.  With the below URL the following text can be found from Microsoft
	            'If you must change the password (strPassword parameter) or the type of authentication (strAuthority parameter) when connecting to WMI, 
	            'then call SWbemLocator.ConnectServer. Note that you can only specify the password and authority in connections to remote computers.
	            'Attempting to set these in a script that is running on the local computer results in a error.
	            Call Wscript.Echo("Attempting a local connection for WMI.  This means even if a user name and")
	            Call Wscript.Echo("password have been supplied these values are being ignored.")
	            If strOutputType = CONST_VERBOSE_OUTPUT Then
			        Call Wscript.Echo("For more information as to why this is the case please refer to the below URL.")
			        Call Wscript.Echo("http://msdn.microsoft.com/library/default.asp?url=/library/en-us/wmisdk/wmi/constructing_a_moniker_string.asp")
			    End If
			    Call Wscript.Echo("")
		    End If
        End If
        If Not getLocalConnect(strComputer , _
        		       objWMIService) Then
    	    blnConnect = False
        End If
    End If
End Function
'********************************************************************
'* 
'* Function getLocalConnect()
'*
'* Purpose: Creats a connection to the local computer WMI
'*
'* Input:   strComputer		A string with the name of the local 
'*                              computer.
'*
'* Output:  strComptuer		computer name
'*			objWMIService	WMI Object
'* 
'********************************************************************
Private Function getLocalConnect(ByRef strComputer,  _
                            	 ByRef objWMIService)

    ON ERROR RESUME NEXT

    Dim objWshNet
    
    getLocalConnect = True
    
    Set objWMIService = GetObject("winmgmts://" & strComputer)
    
    If Err.Number Then
        Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & " occurred in creating the connection." )
        If Err.Description <> "" Then
            Call Wscript.Echo( "Error description: " & Err.Description & "." )
        End If
        Err.Clear
        getLocalConnect = False     'An error occurred
        Exit Function
    End If
        
End Function
'********************************************************************
'* 
'* Function getRemoteConnect()
'*
'* Purpose: Creats a connection to WMI for a remote computer
'*
'* Input:   strNameSpace	Default name space
'*	    	strUserName		Logon user name
'*	    	strPassword		Logon password
'*
'*
'* Output:  strComputer		computer name
'*			objWMIService	WMI Object
'* 
'********************************************************************
Private Function getRemoteConnect(ByVal strNameSpace, _
                            	  ByVal strUserName,  _
                            	  ByVal strPassword,  _
                            	  ByRef strComputer,  _
                            	  ByRef objWMIService)

    ON ERROR RESUME NEXT

Dim objLocator, objWshNet

    getRemoteConnect = True     'There is no error.
    'Create Locator object to connect to remote CIM object manager
    Set objLocator = CreateObject("WbemScripting.SWbemLocator")
    If Err.Number then
        Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & _
                           " occurred in creating a locator object." )
        If Err.Description <> "" Then
            Call Wscript.Echo( "Error description: " & Err.Description & "." )
        End If
        Err.Clear
        getRemoteConnect = False     'An error occurred
        Exit Function
    End If

    'Connect to the namespace which is either local or remote
    Set objWMIService = objLocator.ConnectServer (strComputer, strNameSpace, strUserName, strPassword)
    ObjWMIService.Security_.impersonationlevel = 3
    If Err.Number Then
        Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & " occurred in connecting to computer " _
           & strComputer & ".")
        If Err.Description <> "" Then
            Call Wscript.Echo( "Error description: " & Err.Description & "." )
        End If
        Err.Clear
        GetRemoteConnect = False     'An error occurred
    End If
        
End Function
'********************************************************************
'* 
'* Function getLocalComputerName
'*
'* Purpose: Gets the local computer name
'*
'* Input:
'*
'* Output:  strLocalComputer		sets local computer name
'* 
'********************************************************************
Private Function getLocalComputerName(ByRef strLocalComputer)

	ON ERROR RESUME Next
	
	Dim objWshNet

    Set objWshNet = CreateObject("Wscript.Network")
    strLocalComputer = objWshNet.ComputerName
	If Err.Number Then
    	Call Wscript.Echo( "Error 0x" & CStr(Hex(Err.Number)) & _
    		" occurred in attempting to retrieve the local computer name.  When" )
		Call Wscript.Echo( "this error occurs the process will continue to run against the local machine ")
		Call Wscript.Echo( "only and not a remote computer if it was specified.")
       	If Err.Description <> "" Then
       		Call Wscript.Echo( "Error description: " & Err.Description & "." )
       	End If
       	Err.Clear
       	Exit Function
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
    Wscript.Echo "Provides a warning for low disk space usage on the computer."
    Wscript.Echo ""
    Wscript.Echo "SYNTAX:"
    Wscript.Echo "  DriveSpace.vbs"
    Wscript.Echo "              [/C <computername>][/O <outputfile>][/Q <outputtype>"
    Wscript.Echo "              [/U <username>][/W <password>][/T <threshhold>]"
    Wscript.Echo "              [/D <driveletters>]"
    Wscript.Echo ""
    Wscript.Echo ""
    Wscript.Echo "PARAMETER SPECIFIERS:"
    Wscript.Echo "   /L            Provides a list only of the availabe drive space for the "
    Wscript.Echo "                 selected drives."
    Wscript.Echo ""
    Wscript.Echo "   /E            Used to not have the %errorlevel% set."
    Wscript.Echo ""
    Wscript.Echo "   /S            Timestamp will be listed"
    Wscript.Echo ""
    Wscript.Echo "   computername  The name of the comptuer to check for available diskspace."
    Wscript.Echo ""
    Wscript.Echo "   threshhold    Specify the amount in MB which constitutes a drive with low"
    Wscript.Echo "                 diskspace."
    Wscript.Echo "                 The defualt threshold is " & CONST_DEFAULT_THRESHHOLD & "MB."
    Wscript.Echo ""
    Wscript.Echo "   username      The current user's name."
    Wscript.Echo ""
    Wscript.Echo "   password      Password of the current user."
    Wscript.Echo ""
    Wscript.Echo "   outputfile    The output file name."
    Wscript.Echo ""
    Wscript.Echo "   outputtype    One of Bare, Standard, Verbose"
    Wscript.Echo ""
    Wscript.Echo "EXAMPLE:"
    Wscript.Echo "1. cscript DriveSpace.vbs"
    Wscript.Echo "   	Provides a warning if any of the drives have less than " & CONST_DEFAULT_THRESHHOLD & "MB."
    Wscript.Echo ""
    Wscript.Echo "2. cscript DriveSpace.vbs /C MyMachine2 /T 1000 /Q Verbose"
    Wscript.Echo "      Check the computer MyMachine2 to see if it has less than"
    Wscript.Echo "      1,000MB of free space with verbose output."
    Wscript.Echo ""

End Sub

'********************************************************************
'* 
'*  Function DriveLetters
'*
'*  Purpose: To parse the driveletter string into a string with unique
'*		characters and no spaces
'* 
'*	Input: strDriveLetters	The drive letter string from intParseCmdLine
'*
'*
'*	Output: strDriveLetters	Revised drive letter string with unique
'*							characters and no spaces 
'*  
'********************************************************************
Private Function DriveLetters(ByRef strDriveLetters)

	ON ERROR RESUME NEXT
	
	Dim strTestString
	Dim numStrLength
	Dim strTempString
	Dim strLetter
	Dim numStrTempLength
	Dim numCharPosition
	Dim strTemp
	Dim i, j
	Dim arrBaseString()
	
	
	
	strTestString = LCase(strDriveLetters)
	numStrLength = Len(strTestString)
	strTempString = ""
	
	For i = 0 to (numStrLength - 1)
		strLetter = Mid(strTestString, i + 1, 1)
	    If Asc(strLetter) > 96 AND Asc(strLetter) < 123 Then '97 is "a" and 122 is "z"
	    	numCharPosition = InStr(strTempString, strLetter)
		    If numCharPosition = 0 Then    	
		    	strTempString = strTempString & strLetter
		    End If
		End If
	Next
	
	numStrTempLength = Len(strTempString)
	ReDim arrBaseString(numStrTempLength - 1)
	For i = 0 to (numStrTempLength - 1)
		strLetter = Mid(strTempString, i+1, 1)
		arrBaseString(i) = strLetter
	Next

	'Bubble Sort	
	For i = UBound(arrBaseString) - 1 to 0 Step -1
	    For j= 0 to i
	        If arrBaseString(j) > arrBaseString(j + 1) Then
	            strTemp=arrBaseString(j + 1)
	            arrBaseString(j + 1) = arrBaseString(j)
	            arrBaseString(j)=strTemp
	        End If
	    Next
	Next
	
	'Creates a comma delimited string of characters
	strDriveLetters = ""
	For i = 0 to (numStrTempLength - 1)
		If NOT i = (numStrTempLength - 1) Then
			strDriveLetters = (strDriveLetters & arrBaseString(i) & ",")
		Else
			strDriveLetters = (strDriveLetters & arrBaseString(i))
		End If
	Next

End Function	
'********************************************************************
'*
'* Function GetDriveInformation()
'*
'* Purpose: Populates properties for the drives into arrays
'*
'* Input:	strDriveLetters			Should be a comma delimited string of 
'*									alphabetical characters.
'*			objWMIService			WMI object
'*
'* Output:  arrNonLogicalDiskDriveLetters	stores the drive letters
'*											that are not logical drives
'*											and drives that don't exist
'*											on the computer
'*			arrInvalidDriveType				store the 'type' of drive
'*											that is not valid
'*			arrValidDriveLetters			stores specified drive letters
'*											that exist on the pc and are
'*											logical disk
'*			arrInvalidDriveLetters			stores all logical disk letters
'*											that exist
'*			arrNoDrive						stores drive letters that
'*											do not exist
'*			arrLogicalDiskDriveLetters		stores drive letters for all
'*											logical disk that exist on the PC
'*			arrDriveFreeSpace				stores the drive freespace in MB of
'*											all logical disk on the PC
'*
'********************************************************************
Private Function GetDriveInformation(objWMIService						,	_
									ByRef arrNonLogicalDiskDriveLetters	, 	_
									ByRef arrInvalidDriveType			,	_
									ByRef arrValidDriveLetters			,	_
									ByRef arrInvalidDriveLetters		,	_
									ByRef arrNoDrive					,	_
									ByRef arrLogicalDiskDriveLetters	,	_
									ByRef arrDriveFreeSpace				,	_
									ByVal strDriveLetters)

									
	ON ERROR RESUME NEXT
	
	Dim objDisk
	Dim colDisks
	Dim strDiskFound
	Dim strFreeMegaBytes
	Dim strDeviceId
	Dim	i, j, k, l, m																'used for counters and loops
	Dim arrDriveLetters																'array that stores specified desired drive letters
	Dim strInvalidDriveLetters
	Dim strNoDrive

	

	'Gets the logical disk drive information
	i = 0 'Counter for logical disk
	j = 0 'Counter for non logical disk
	Set colDisks = objWMIService.ExecQuery ("Select * from Win32_LogicalDisk")
	For Each objDisk in colDisks
	    Select Case objDisk.DriveType
			Case 1
				'No root directory
				ReDim Preserve arrNonLogicalDiskDriveLetters(j)
				arrNonLogicalDiskDriveLetters(j) = LCase(Left(objDisk.DeviceID, 1))
				ReDim Preserve arrInvalidDriveType(j)
				arrInvalidDriveType(j) = "No Root Directory"
				j= (j + 1)
			Case 2
				'Removable Disk
				ReDim Preserve arrNonLogicalDiskDriveLetters(j)
				arrNonLogicalDiskDriveLetters(j) = LCase(Left(objDisk.DeviceID, 1))
				ReDim Preserve arrInvalidDriveType(j)
				arrInvalidDriveType(j) = "Removable Disk"
				j= (j + 1)
			Case 3
				'Logical Disk
				strFreeMegaBytes = FormatNumber((objDisk.FreeSpace / CONST_CONVERSION_FACTOR),	_
					CONST_NO_DECIMAL_PLACES, CONST_LEADING_ZERO, CONST_NEGS_PARENTHESIS, CONST_NO_GROUP_DIGITS)
				ReDim Preserve arrDriveFreeSpace(i)
				arrDriveFreeSpace(i) = strFreeMegaBytes
				strDeviceId = LCase(Left(objDisk.DeviceID, 1))
				ReDim Preserve arrLogicalDiskDriveLetters(i)
				arrLogicalDiskDriveLetters(i) = strDeviceId
				i = (i + 1)				
			Case 4
				'Network Disk
				ReDim Preserve arrNonLogicalDiskDriveLetters(j)
				arrNonLogicalDiskDriveLetters(j) = LCase(Left(objDisk.DeviceID, 1))
				ReDim Preserve arrInvalidDriveType(j)
				arrInvalidDriveType(j) = "Network Disk"
				j= (j + 1)     
			Case 5
				'Compact Disk
				ReDim Preserve arrNonLogicalDiskDriveLetters(j)
				arrNonLogicalDiskDriveLetters(j) = LCase(Left(objDisk.DeviceID, 1))
				ReDim Preserve arrInvalidDriveType(j)
				arrInvalidDriveType(j) = "Compact Disk"
				j= (j + 1)    
			Case 6
				'Ram Disk
				ReDim Preserve arrNonLogicalDiskDriveLetters(j)
				arrNonLogicalDiskDriveLetters(j) = LCase(Left(objDisk.DeviceID, 1))
				ReDim Preserve arrInvalidDriveType(j)
				arrInvalidDriveType(j) = "Ram Disk"
				j= (j + 1)
			Case Else
				ReDim Preserve arrNonLogicalDiskDriveLetters(j)
				arrNonLogicalDiskDriveLetters(j) = LCase(Left(objDisk.DeviceID, 1))
				ReDim Preserve arrInvalidDriveType(j)
				arrInvalidDriveType(j) = "Undetermined"
				j= (j + 1)
		End Select
		
	Next
	
	'Compares the logical disk drive information against specified drive letters and creates
	'two new arrays with one being a list of specified valid drive letters and the other being a
	'list of specified not valid drive letters.
	If NOT IsEmpty(strDriveLetters) Then
		arrDriveLetters = Split(strDriveLetters, ",")	
		k = 0
		l = 0
		m = 0
		strInvalidDriveLetters = "False"
		strNoDrive = "False"
		For i = 0 to UBound(arrDriveLetters)
			strDiskFound = "False"
			'Creates an array of valid drive letters that were specified
			For j = 0 to UBound(arrLogicalDiskDriveLetters)
				If arrDriveLetters(i) = arrLogicalDiskDriveLetters(j) Then
					strDiskFound = "True"
					ReDim Preserve arrValidDriveLetters(k)
					arrValidDriveLetters(k) = arrDriveLetters(i)
					k = (k + 1)
				End If
			Next
			If strDiskFound = "False" Then
				For j = 0 to UBound(arrNonLogicalDiskDriveLetters)
					'If the drive is not found as a valid logical disk then it first checks
					'to see if the drive is an existing non logical disk
					If arrDriveLetters(i) = arrNonLogicalDiskDriveLetters(j) Then
						strDiskFound = "True"
						strInvalidDriveLetters = "True"
						ReDim Preserve arrInvalidDriveLetters(l)
						arrInvalidDriveLetters(l) = arrDriveLetters(i)
						l = (l + 1)
					End If
				Next
			End If
			'If the drive is not found at all then it is added to the not found array
			'the non-logical disk array and invalid drive type array
			If strDiskFound = "False" Then
				strNoDrive = "True"
				ReDim Preserve arrNoDrive(m)
				arrNoDrive(m) = arrDriveLetters(i)
				m = (m + 1)
				ReDim Preserve arrNonLogicalDiskDriveLetters(UBound(arrNonLogicalDiskDriveLetters) + 1)
				arrNonLogicalDiskDriveLetters(UBound(arrNonLogicalDiskDriveLetters)) = arrDriveLetters(i)
				ReDim Preserve arrInvalidDriveType(UBound(arrInvalidDriveType) + 1)
				arrInvalidDriveType(UBound(arrInvalidDriveType)) = "Drive does not exist"
			End If
		Next		
	End If
	If strDriveLetters = "" Then
		For i = 0 to UBound(arrLogicalDiskDriveLetters)
			ReDim Preserve arrValidDriveLetters(i)
			arrValidDriveLetters(i) = arrLogicalDiskDriveLetters(i)
		Next
	End If
	If strInvalidDriveLetters = "False" Then
		ReDim arrInvalidDriveLetters(0)
		arrInvalidDriveLetters(0) = "Null"
	End If
	If strNoDrive = "False" Then
		ReDim arrNoDrive(0)
		arrNoDrive(0) = "Null"
	End If
	
End Function
'********************************************************************
'*
'* Function Comparison()
'*
'* Purpose: Compares values to see if they meet the available disk
'*			space requirement.
'*
'* Input:	arrDriveFreeSpace			free space for the logical disk
'*			arrLogicalDiskDriveLetters	all drive letters that exist
'*			arrValidDriveLetters		drive letters of logical disk
'*										that exist
'*			
'*
'* Output:	strThreshhold				comparison value
'*			arrDriveStatus				array of comparison results
'*
'********************************************************************
Private Function Comparison(ByVal arrDriveFreeSpace			, _
							ByVal arrLogicalDiskDriveLetters, _
							ByVal arrValidDriveLetters		, _
							ByRef strThreshhold				, _
							ByRef arrDriveStatus)
								
	ON ERROR RESUME NEXT
	
	
	Dim strDiskFound
	Dim	i, j																		'used for counters and loops
	
	
	If IsEmpty(strThreshhold) Then
		strThreshhold = CONST_DEFAULT_THRESHHOLD
	End If
	
	arrDriveFreeSpace = Split(strDriveFreeSpace, ",")
	arrLogicalDiskDriveLetters = Split(strLogicalDiskDriveLetters, ",")
	arrValidDriveLetters = Split(strValidDriveLetters, ",")
	
	
	strDriveStatus = ""
	For i = 0 to UBound(arrValidDriveLetters)
		strDiskFound = "False"
		j = 0
		Do Until strDiskFound = "True"
			If arrValidDriveLetters(i) = arrLogicalDiskDriveLetters(j) Then
				strDiskFound = "True"
				ReDim Preserve arrDriveStatus(i)
				If CLng(arrDriveFreeSpace(j)) < CLng(strThreshhold) Then
					arrDriveStatus(i) = CONST_FAILED
				Else
					arrDriveStatus(i) = CONST_PASSED
				End If
			End If
			j = (j + 1)
		Loop
		If NOT i = UBound(arrValidDriveLetters) Then
			strDriveStatus = (strDriveStatus & arrDriveStatus(i) & ",")
		Else
			strDriveStatus = (strDriveStatus & arrDriveStatus(i))
		End If
	Next
	
End Function
'********************************************************************
'*
'* Sub      WriteOutput()
'*
'* Purpose: Writes the output
'*
'* Input:   strOutputType					type of output selected
'*			strDriveLetters					specified drive letters
'*			strThreshhold					comparison value
'*			strTaskCommand					one of /list /check
'*			strTimeStamp					timestamp option
'*			strOutputFile					output file path
'*			strErrorLevel					errorlevel option
'*			strComputer						computer name
'*			strUserName						user name
'*			strPassword						password
'*			arrDriveFreeSpace				array of drive free space in MB
'*			arrNonLogicalDiskDriveLetters	array of all non logical disk
'*											drive specified
'*			arrInvalidDriveType				the 'type' of invalid drive
'*			arrValidDriveLetters			accepted specified drive letters
'*			arrInvalidDriveLetters			existing non logical disk drives
'*			arrNoDrive						non existing specified drives
'*			arrLogicalDiskDriveLetters		array of all logicak disk drives
'*			arrDriveStatus					comparison results
'*			objOutputFile					file object
'*
'* Output:  Drive information
'*
'********************************************************************
Private Sub WriteOutput(strOutputType					,_
						strDriveLetters					,_
						strThreshhold					,_
						strTaskCommand					,_
						strTimeStamp					,_
						strOutputFile					,_
						strErrorLevel					,_
						strComputer						,_
						strUserName						,_
						strPassword						,_
						arrDriveFreeSpace				,_
						arrNonLogicalDiskDriveLetters	,_
						arrInvalidDriveType				,_
						arrValidDriveLetters			,_
						arrInvalidDriveLetters			,_
						arrNoDrive						,_
						arrLogicalDiskDriveLetters		,_
						arrDriveStatus					,_
						objOutputFile)
						
	ON ERROR RESUME NEXT

	
	Dim i
	Dim arrOutput()											'output array	
	
	If strOutputType = CONST_VERBOSE_OUTPUT Then
		Call WriteVerboseOutput(arrDriveFreeSpace				,_
								arrNonLogicalDiskDriveLetters	,_
								arrInvalidDriveType				,_
								arrValidDriveLetters			,_
								arrInvalidDriveLetters			,_
								arrNoDrive						,_
								arrLogicalDiskDriveLetters		,_
								arrDriveStatus					,_
								strDriveLetters					,_
								strThreshhold					,_
								strErrorLevel					,_
								strOutputType					,_
								strTimeStamp					,_
								strOutputFile					,_
								strTaskCommand					,_
								strComputer						,_
								strUserName						,_
								strPassword						,_
								arrOutput)		
	End If
	
	If strOutputType = CONST_STANDARD_OUTPUT Then
		Call WriteStandardOutput(arrDriveFreeSpace				,_
								arrNonLogicalDiskDriveLetters	,_
								arrInvalidDriveType				,_
								arrValidDriveLetters			,_
								arrInvalidDriveLetters			,_
								arrNoDrive						,_
								arrLogicalDiskDriveLetters		,_
								arrDriveStatus					,_
								strDriveLetters					,_
								strTaskCommand					,_
								arrOutput)		
	End If
	If strOutputType = CONST_BARE_OUTPUT Then
		Call WriteBareOutput(arrDriveFreeSpace					,_
								arrValidDriveLetters			,_
								arrLogicalDiskDriveLetters		,_
								arrOutput)		
	End If
	
	'Dispaly output
	If strTimeStamp = CONST_TIMESTAMP_ON Then
		For i = 0 to UBound(arrOutput)
			Call WScript.echo (Now & Space(3) & arrOutput(i))
		Next
	Else	
		For i = 0 to UBound(arrOutput)
			Call WScript.echo (arrOutput(i))
		Next
	End If
	
	'Write to file the same output
	If NOT IsEmpty(strOutputFile) Then
		If strTimeStamp = CONST_TIMESTAMP_ON Then
			For i = 0 to UBound(arrOutput)
				objOutputFile.WriteLine	(Now & Space(3) & arrOutput(i))
			Next
		Else
			For i = 0 to UBound(arrOutput)
				objOutputFile.WriteLine	(arrOutput(i))
			Next 
		End If
		If NOT IsEmpty(objOutputFile) Then
        objOutputFile.Close
        	If NOT strOutputType = CONST_BARE_OUTPUT Then
            	Call Wscript.Echo ("Results are saved in file " & strOutputFile & ".")
        	End If
        End If
    End If
			
	
End Sub
'********************************************************************
'*
'* Function WriteVerboseOutput
'*
'* Purpose: Builds the array arrOutput
'*
'* Input:   arrDriveFreeSpace				array of drive free space in MB
'*			arrNonLogicalDiskDriveLetters	array of all non logical disk
'*											drive specified
'*			arrInvalidDriveType				the 'type' of invalid drive
'*			arrValidDriveLetters			accepted specified drive letters
'*			arrInvalidDriveLetters			existing non logical disk drives
'*			arrNoDrive						non existing specified drives
'*			arrLogicalDiskDriveLetters		array of all logicak disk drives
'*			arrDriveStatus					comparison results
'*			strDriveLetters					specified drive letters
'*			strThreshhold					comparison value
'*			strErrorLevel					errorlevel option
'*			strOutputType					type of output selected
'*			strTimeStamp					timestamp option
'*			strOutputFile					output file path
'*			strTaskCommand					one of /list /check
'*			strComputer						computer name
'*			strUserName						user name
'*			strPassword						password
'*
'* Output:  arrOutput						array of all output lines
'*
'********************************************************************
Private Function WriteVerboseOutput(ByVal arrDriveFreeSpace				,_
									ByVal arrNonLogicalDiskDriveLetters	,_
									ByVal arrInvalidDriveType			,_
									ByVal arrValidDriveLetters			,_
									ByVal arrInvalidDriveLetters		,_
									ByVal arrNoDrive					,_
									ByVal arrLogicalDiskDriveLetters	,_
									ByVal arrDriveStatus				,_
									ByVal strDriveLetters				,_
									ByVal strThreshhold					,_
									ByVal strErrorLevel					,_
									ByVal strOutputType					,_
									ByVal strTimeStamp					,_
									ByVal strOutputFile					,_
									ByVal strTaskCommand				,_
									ByVal strComputer					,_
									ByVal strUserName					,_
									ByVal strPassword					,_
									ByRef arrOutput)
									
	ON ERROR RESUME NEXT
	
	
	Dim i
	
	i = 0
	ReDim arrOutput(11)									
	arrOutput(i) = ("DriveSpace.vbs")
	i = (i + 1)
	
	arrOutput(i) = ("The following options have been selected:")
	i = (i + 1)
	
	arrOutput(i) = ("Output = " & strOutputType)
	i = (i + 1)
	
	If NOT IsEmpty(strOutputFile) Then
		arrOutput(i) = ("Output file = " & strOutputFile)
	Else
		arrOutput(i) = ("Output file = OPTION NOT SELECTED")
	End If
	i = (i + 1)
	
	arrOutput(i) = ("Computer Name = " & strComputer)
	i = (i + 1)
	
	If NOT IsEmpty(strUserName) Then
		arrOutput(i) = ("User name = " & strUserName)
	Else
		arrOutput(i) = ("User name = NOT SELECTED")
	End If
	i = (i + 1)
	
	If NOT IsEmpty(strPassword) Then
		arrOutput(i) = ("Password = " & strPassword)
	Else
		arrOutput(i) = ("Password = NOT SELECTED")
	End If
	i = (i + 1)
	
	arrOutput(i) = ("Set error level = " & strErrorLevel)
	i = (i + 1)
	
	If strTaskCommand = "List" Then
		arrOutput(i) = ("Comparison value = OPTION NOT AVAILABLE")
	Else
		arrOutput(i)= ("Comparison value = " & FormatNumber(strThreshhold, CONST_NO_DECIMAL_PLACES) & "MB")
	End If
	i = (i + 1)
	
	If strTaskCommand = "List" Then
		arrOutput(i) = ("List option = On")
	Else
		arrOutput(i) = ("List option = Off")
	End If
	i = (i + 1)
	
	arrOutput(i) = ("")
	i = (i + 1)
	
	arrOutput(i) = ("")
	i = (i + 1)
	
	Call CreateTables(arrDriveFreeSpace					,_
						arrNonLogicalDiskDriveLetters	,_
						arrInvalidDriveType				,_
						arrValidDriveLetters			,_
						arrInvalidDriveLetters			,_
						arrNoDrive						,_
						arrLogicalDiskDriveLetters		,_
						arrDriveStatus					,_
						strDriveLetters					,_
						strTaskCommand					,_
						arrOutput						,_
						i)
	
			
End Function
'********************************************************************
'*
'* Function WriteStandardOutput
'*
'* Purpose: Builds the array arrOutput
'*
'* Input:   arrDriveFreeSpace				array of drive free space in MB
'*			arrNonLogicalDiskDriveLetters	array of all non logical disk
'*											drive specified
'*			arrInvalidDriveType				the 'type' of invalid drive
'*			arrValidDriveLetters			accepted specified drive letters
'*			arrInvalidDriveLetters			existing non logical disk drives
'*			arrNoDrive						non existing specified drives
'*			arrLogicalDiskDriveLetters		array of all logicak disk drives
'*			arrDriveStatus					comparison results
'*			strDriveLetters					specified drive letters
'*			strTaskCommand					one of /list /check
'*
'* Output:  arrOutput						array of all output lines
'*
'********************************************************************
Private Function WriteStandardOutput(ByVal arrDriveFreeSpace			,_
								ByVal arrNonLogicalDiskDriveLetters		,_
								ByVal arrInvalidDriveType				,_
								ByVal arrValidDriveLetters				,_
								ByVal arrInvalidDriveLetters		,_
								ByVal arrNoDrive					,_
								ByVal arrLogicalDiskDriveLetters		,_
								ByVal arrDriveStatus					,_
								ByVal strDriveLetters					,_
								ByVal strTaskCommand					,_
								ByRef arrOutput)
								
	ON ERROR RESUME NEXT
									
	Dim strDriveFound
	Dim i
		
	i = 0
	ReDim arrOutput(i)									
	arrOutput(i) = ("DriveSpace.vbs")
	i = (i + 1)
	
	Call CreateTables(arrDriveFreeSpace					,_
						arrNonLogicalDiskDriveLetters	,_
						arrInvalidDriveType				,_
						arrValidDriveLetters			,_
						arrInvalidDriveLetters			,_
						arrNoDrive						,_
						arrLogicalDiskDriveLetters		,_
						arrDriveStatus					,_
						strDriveLetters					,_
						strTaskCommand					,_
						arrOutput						,_
						i)
	
								
End Function
'********************************************************************
'*
'* Function WriteBareOutput
'*
'* Purpose: Builds the array arrOutput
'*
'* Input:   arrDriveFreeSpace				array of drive free space in MB
'*			arrValidDriveLetters			accepted specified drive letters
'*			arrLogicalDiskDriveLetters		array of all logicak disk drives
'*
'* Output:  arrOutput						array of all output lines
'*
'********************************************************************	
Private Function WriteBareOutput(ByVal arrDriveFreeSpace			,_
								ByVal arrValidDriveLetters			,_
								ByVal arrLogicalDiskDriveLetters	,_
								ByRef arrOutput)
								
	ON ERROR RESUME NEXT	
	
	Dim strDriveFound
	Dim i
	Dim j
	Dim k
		
	i = 0	
	
	If NOT IsEmpty(arrValidDriveLetters(0)) Then
		ReDim Preserve arrOutput(i + UBound(arrValidDriveLetters))
		For j = 0 to UBound(arrValidDriveLetters)
			k = 0
			strDriveFound = "False"
			Do Until strDriveFound = "True"
				If arrValidDriveLetters(j) = arrLogicalDiskDriveLetters(k) Then
					strDriveFound = "True"
				End If
				k = (k + 1)
			Loop
			arrOutput(i) = (UCase(arrValidDriveLetters(j)) & Space(1) & FormatNumber(arrDriveFreeSpace(k - 1), _
				CONST_NO_DECIMAL_PLACES, CONST_LEADING_ZERO, CONST_NEGS_PARENTHESIS, CONST_NO_GROUP_DIGITS))
			i = (i + 1)
		Next		
	Else
		ReDim Preserve arrOutput(i + 1)
		arrOutput(i) = ("None of the specified drive letters are valid drives to list.")
		i = (i + 1)
	End If
	
	
End Function
'********************************************************************
'*
'* Function CreateTables
'*
'* Purpose: Builds table output
'*
'* Input:   arrDriveFreeSpace				array of drive free space in MB
'*			arrNonLogicalDiskDriveLetters	array of all non logical disk
'*											drive specified
'*			arrInvalidDriveType				the 'type' of invalid drive
'*			arrValidDriveLetters			accepted specified drive letters
'*			arrInvalidDriveLetters			existing non logical disk drives
'*			arrNoDrive						non existing specified drives
'*			arrLogicalDiskDriveLetters		array of all logicak disk drives
'*			arrDriveStatus					comparison results
'*			strDriveLetters					specified drive letters
'*			strTaskCommand					one of /list /check
'*
'* Output:  arrOutput						array of all output lines
'*			i								counter for the output array
'*
'********************************************************************
Private Function CreateTables(ByVal arrDriveFreeSpace				,_
							ByVal arrNonLogicalDiskDriveLetters		,_
							ByVal arrInvalidDriveType				,_
							ByVal arrValidDriveLetters				,_
							ByVal arrInvalidDriveLetters			,_
							ByVal arrNoDrive						,_
							ByVal arrLogicalDiskDriveLetters		,_
							ByVal arrDriveStatus					,_
							ByVal strDriveLetters					,_
							ByVal strTaskCommand					,_
							ByRef arrOutput							,_
							ByRef i)
							
	ON ERROR RESUME NEXT
	
	Dim strDriveFound
	Dim j
	Dim k
	
	'List all non logical disk drives
	If NOT IsEmpty(arrNonLogicalDiskDriveLetters(0)) Then
		If strDriveLetters = "" Then
			ReDim Preserve arrOutput(i + 4 + UBound(arrNonLogicalDiskDriveLetters))
			arrOutput(i) = ("The following specified drives are not valid.")
			i = (i + 1)
			arrOutput(i) = ("DRIVE LETTER" & Space(15) & "DRIVE TYPE")
			i = (i + 1)
			For j = 0 to UBound(arrNonLogicalDiskDriveLetters)
				arrOutput(i) = ("" & UCase(arrNonLogicalDiskDriveLetters(j))& Space(26) & arrInvalidDriveType(j))
				i = (i + 1)
			Next
			arrOutput(i) = ("")
			i = (i + 1)
		
			arrOutput(i) = ("")
			i = (i + 1)
		Else
			If NOT arrInvalidDriveLetters(0) = "Null" OR NOT arrNoDrive(0) = "Null" Then
				ReDim Preserve arrOutput(i + 4 + UBound(arrInvalidDriveLetters) + UBound(arrNoDrive))
				arrOutput(i) = ("The following specified drives are not valid.")
				i = (i + 1)
				arrOutput(i) = ("DRIVE LETTER" & Space(15) & "DRIVE TYPE")
				i = (i + 1)
				If NOT arrInvalidDriveLetters(0) = "Null" Then
					For j = 0 to UBound(arrInvalidDriveLetters)
						k = 0
						strDriveFound = "False"
						Do Until (strDriveFound = "True") OR (k = (UBound(arrNonLogicalDiskDriveLetters) + 1))
							If arrInvalidDriveLetters(j) = arrNonLogicalDiskDriveLetters(k) Then
								strDriveFound = "True"
							End If
							k = (k + 1)
						Loop
						If strDriveFound = "True" Then
							arrOutput(i) = (UCase(arrInvalidDriveLetters(j))& Space(26) & arrInvalidDriveType(k - 1))
							i = (i + 1)
						End If
					Next
				End If
				If NOT arrNoDrive(0) = "Null" Then
					For j = 0 to UBound(arrNoDrive)
						k = 0
						strDriveFound = "False"
						Do Until (strDriveFound = "True") OR (k = (UBound(arrNonLogicalDiskDriveLetters) + 1))
							If arrNoDrive(j) = arrNonLogicalDiskDriveLetters(k) Then
								strDriveFound = "True"
							End If
							k = (k + 1)
						Loop
						If strDriveFound = "True" Then
							arrOutput(i) = (UCase(arrNoDrive(j))& Space(26) & arrInvalidDriveType(k - 1))
							i = (i + 1)
						End If
					Next
				End If
				arrOutput(i) = ("")
				i = (i + 1)
			
				arrOutput(i) = ("")
				i = (i + 1)
			End If
		End If
	End If
			
	
	If NOT IsEmpty(arrValidDriveLetters(0)) Then
		ReDim Preserve arrOutput(i + 2 + UBound(arrValidDriveLetters))
		arrOutput(i) = ("Listing the valid specified drive letters.")
		i = (i + 1)
		If strTaskCommand = "List" Then
			arrOutput(i) = ("DRIVE LETTER" & Space(15) & "DRIVE FREESPACE")
			i = (i + 1)
			For j = 0 to UBound(arrValidDriveLetters)
				k = 0
				strDriveFound = "False"
				Do Until strDriveFound = "True"
					If arrValidDriveLetters(j) = arrLogicalDiskDriveLetters(k) Then
						strDriveFound = "True"
					End If
					k = (k + 1)
				Loop
				arrOutput(i) = (UCase(arrValidDriveLetters(j)) & Space(26) & FormatNumber(arrDriveFreeSpace(k - 1), CONST_NO_DECIMAL_PLACES) & "MB")
				i = (i + 1)
			Next
			Else If strTaskCommand = "Check" Then
				arrOutput(i) = ("DRIVE LETTER" & Space(10) & "COMPARISON CHECK" & Space(8) & "DRIVE FREESPACE")
				i = (i + 1)
				For j = 0 to UBound(arrValidDriveLetters)
				k = 0
				strDriveFound = "False"
				Do Until strDriveFound = "True"
					If arrValidDriveLetters(j) = arrLogicalDiskDriveLetters(k) Then
						strDriveFound = "True"
					End If
					k = (k + 1)
				Loop
				arrOutput(i) = (UCase(arrValidDriveLetters(j)) & Space(21) & arrDriveStatus(j) & Space(18) & FormatNumber(arrDriveFreeSpace(k - 1), CONST_NO_DECIMAL_PLACES) & "MB")
				i = (i + 1)
				Next
			End If
		End If		
	Else
		ReDim Preserve arrOutput(i + 1)
		arrOutput(i) = ("None of the specified drive letters are valid drives to list.")
		i = (i + 1)
	End If
	
End Function