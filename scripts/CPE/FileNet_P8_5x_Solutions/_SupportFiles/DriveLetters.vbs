'********************************************************************
'*
'* File:           DriveLetters.vbs
'* Created:        November 2006
'*
'* Main Function:
'*
'*
'* Portions of this script are from the MSDN website.  Microsoft has
'* given permission on the website for use of their examples.
'*
'* Copyright (C) 2006 Saber
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
Const CONST_USED                    = "used"					'Default action
Const CONST_UNUSED                  = "unused"
Const CONST_BARE_OUTPUT             = "bare"
Const CONST_STANDARD_OUTPUT         = "standard"
Const CONST_TIMESTAMP_ON            = "On"
Const CONST_TIMESTAMP_OFF           = "Off"


'Declare variables
Dim intOpMode
Dim strOutputFile, strOutputType, strTimeStamp
Dim strComputer, strUserName, strPassword, strTaskCommand

'Make sure the host is cscript, if not then abort
VerifyHostIsCscript()

'Parse the command line

intOpMode = intParseCmdLine(strComputer, _
                            strUserName, _
                            strPassword, _
                            strOutputFile, _
                            strOutputType, _
                            strTimeStamp, _
                            strTaskCommand)

Select Case intOpMode

  Case CONST_SHOW_USAGE
    Call ShowUsage()

  Case CONST_PROCEED
    Call CheckDriveLetters(strComputer, _
                           strUserName, _
                           strPassword, _
                           strOutputFile, _
                           strOutputType, _
                           strTimeStamp, _
                           strTaskCommand)


  Case CONST_ERROR
    Wscript.Echo ("Error occurred in passing parameters.")
    Wscript.Quit(1)

  Case Else                    'Default -- should never happen
    Call Print("Error occurred in passing parameters.")
    Wscript.Quit(1)

End Select
'********************************************************************
'*
'* Sub      CheckDriveLetters()
'*
'* Purpose: Checks drives for the computer
'*
'* Input:   strComputer     Computer Name
'*          strUserName     User name to connect as
'*          strPassword     Password for the user
'*          strOutputFile   Output file path and name
'*          strOutputType   Output display type
'*          strTimeStamp    Timestamp option
'*          strTaskCommand  one of used or unused
'*
'* Output:  Displays drive information
'*
'********************************************************************
Private Sub CheckDriveLetters(strComputer, _
                              strUserName, _
                              strPassword, _
                              strOutputFile, _
                              strOutputType, _
                              strTimeStamp, _
                              strTaskCommand)

ON ERROR RESUME NEXT

Dim objFileSystem, objOutputFile, objWMIService

'Open a text file for output if the file is requested
If Not IsEmpty(strOutputFile) Then
  If (Not blnOpenFile(strOutputFile, objOutputFile)) Then
    Wscript.Echo "Could not open an output file."
    Exit Sub
  End If
End If

'Establish a connection with the computer.
If Not blnConnect(strUserName, _
                  strPassword, _
                  strOutputType, _
                  strComputer, _
                  objWMIService) Then
  Wscript.Echo ""
  Wscript.Echo "Error occured connecting to WMI for " & strComputer
  Exit Sub
End If

Call ExecuteMethod(objWMIService, _
                   objOutputFile, _
                   strTaskCommand, _
                   strOutputType, _
                   strComputer, _
                   strUserName, _
                   strOutputFile, _
                   strTimeStamp)

End Sub
'********************************************************************
'*
'* Sub      ExecuteMethod()
'*
'* Purpose: Checks drive letters
'*
'* Input:   objWMIService	  WMI Object
'*          objOutputFile	  Output file
'*          strTaskCommand	one of used or unused
'*          strOutputType	  Omits header info if set
'*          strComputer		  Computer name
'*          strUserName		  User name
'*          strOutputFile	  Output file path
'*          strTimeStamp	  Time Stamp option
'*
'* Output:  Drive letter information
'*
'********************************************************************
Sub ExecuteMethod	(objWMIService, _
                   objOutputFile, _
                	 strTaskCommand, _
                	 strOutputType, _
                	 strComputer, _
                	 strUserName, _
                	 strOutputFile, _
                	 strTimeStamp)


Dim i
Dim arrDriveLetters()                          'stores specified drive letters that exist on the pc
Dim arrUnusedDriveLetters()                    'array of unused drive letters
Dim arrDriveType()                             'stores the 'type' of drive

Call GetDriveInformation(objWMIService, _
                         arrDriveLetters, _
                         arrDriveType)

If strTaskCommand = CONST_UNUSED Then
  Call GetUnusedDriveLetters(arrDriveLetters, _
                             arrUnusedDriveLetters)
End If

Call WriteOutput(strOutputType, _
                 strTaskCommand, _
                 strTimeStamp, _
                 strOutputFile, _
                 strComputer, _
                 strUserName, _
                 arrDriveLetters, _
                 arrDriveType, _
                 arrUnusedDriveLetters, _
                 objOutputFile)

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
'*          strOutputType       output display type
'*          strTimeStamp        timestamp option
'*          strTaskCommand      one of list used drive letters, unused drive letters
'*
'********************************************************************
Private Function intParseCmdLine(ByRef strComputer, _
                                 ByRef strUserName, _
                                 ByRef strPassword, _
                                 ByRef strOutputFile, _
                                 ByRef strOutputType, _
                                 ByRef strTimeStamp, _
                                 ByRef strTaskCommand)

ON ERROR RESUME NEXT

Dim strFlag
Dim intState, intArgIter
Dim objFileSystem

If Wscript.Arguments.Count > 0 Then
  strFlag = Wscript.arguments.Item(0)
End If

If IsEmpty(strFlag) Then                'No arguments have been received
  intParseCmdLine = CONST_PROCEED
  strTaskCommand = CONST_USED
  strTimeStamp = CONST_TIMESTAMP_OFF
  strOutputType = CONST_STANDARD_OUTPUT
  strUserName = "Logged on user"
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

    Case "/a"
    intParseCmdLine = CONST_PROCEED
    strTaskCommand = CONST_UNUSED
    intArgIter = intArgIter + 1

    Case "/c"
    If Not blnGetArg("Computer", strComputer, intArgIter) Then
      intParseCmdLine = CONST_ERROR
      Exit Function
    End If
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
    Wscript.Echo "Invalid or misplaced parameter: " _
    & Wscript.arguments.Item(intArgIter) & vbCRLF _
    & "Please check the input and try again," & vbCRLF _
    & "or invoke with '/?' for help with the syntax."
    Wscript.Quit

  End Select

Loop '** intArgIter <= Wscript.arguments.Count - 1

'Makes sure the strOutputType argument is acceptable
If Not IsEmpty(strOutputType) Then
  strOutputType = LCase(strOutputType)
  If Not strOutputType = CONST_BARE_OUTPUT Then
    If Not strOutputType = CONST_STANDARD_OUTPUT Then
        IntParseCmdLine = CONST_ERROR
        Exit Function
    End If
  End If
Else
  strOutputType = CONST_STANDARD_OUTPUT
End If

If IsEmpty(strTimeStamp) Then
  strTimeStamp = CONST_TIMESTAMP_OFF
End If

If IsEmpty(strUserName) Then
  struserName = "Logged on user"
End If

If IsEmpty(strTaskCommand) Then
  intParseCmdLine = CONST_PROCEED
  strTaskCommand = CONST_USED
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
Private Function blnGetArg (ByVal StrVarName, _
                            ByRef strVar, _
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
'* Output:  objWMIService   is returned  as a service object.
'*          strComputer     is set to local host if left unspecified
'*
'********************************************************************
Private Function blnConnect(ByVal strUserName, _
                            ByVal strPassword, _
                            ByVal strOutputType, _
                            ByRef strComputer, _
                            ByRef objWMIService)

ON ERROR RESUME NEXT

Dim strLocalComputer

blnConnect = True

Call getLocalComputerName(strLocalComputer)
If IsEmpty(strComputer) Then
  strComputer = strLocalComputer
End If

'Test to see if a remote connection is needed
If Not UCase(strLocalComputer) = UCase(strComputer) Then
  If Not getRemoteConnect("root\cimv2", _
                          strUserName, _
                          strPassword, _
                          strComputer, _
                          objWMIService ) Then
    Wscript.Echo ""
    Wscript.Echo "Please check the computer name, credentials and WBEM Core."
    blnConnect = False
    Exit Function
  End If
  'Else a local connection is needed
Else
  If Not IsEmpty(strPassword) Then
    If Not strOutputType = CONST_BARE_OUTPUT Then
      'Note stating limitations of connecting to WMI locally.  With the below URL the following text can be found from Microsoft
      'If you must change the password (strPassword parameter) or the type of authentication (strAuthority parameter) when connecting to WMI,
      'then call SWbemLocator.ConnectServer. Note that you can only specify the password and authority in connections to remote computers.
      'Attempting to set these in a script that is running on the local computer results in a error.
      Wscript.Echo "Attempting a local connection for WMI.  This means even if a user name and"
      Wscript.Echo "password have been supplied these values are being ignored."
      If strOutputType = CONST_VERBOSE_OUTPUT Then
        Wscript.Echo "For more information as to why this is the case please refer to the below URL."
        Wscript.Echo "http://msdn.microsoft.com/library/default.asp?url=/library/en-us/wmisdk/wmi/constructing_a_moniker_string.asp"
      End If
      Wscript.Echo ""
    End If
  End If
  If Not getLocalConnect(strComputer, _
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
'*          objWMIService	WMI Object
'*
'********************************************************************
Private Function getLocalConnect(ByRef strComputer, _
                                 ByRef objWMIService)

ON ERROR RESUME NEXT

Dim objWshNet

getLocalConnect = True

Set objWMIService = GetObject("winmgmts://" & strComputer)

If Err.Number Then
  Wscript.Echo "Error 0x" & CStr(Hex(Err.Number)) & " occurred in creating the connection."
  If Err.Description <> "" Then
    Wscript.Echo "Error description: " & Err.Description & "."
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
'*          strUserName		Logon user name
'*          strPassword		Logon password
'*
'*
'* Output:  strComputer		computer name
'*          objWMIService	WMI Object
'*
'********************************************************************
Private Function getRemoteConnect(ByVal strNameSpace, _
                            	    ByVal strUserName, _
                            	    ByVal strPassword, _
                            	    ByRef strComputer, _
                            	    ByRef objWMIService)

ON ERROR RESUME NEXT

Dim objLocator, objWshNet

getRemoteConnect = True     'There is no error.
'Create Locator object to connect to remote CIM object manager
Set objLocator = CreateObject("WbemScripting.SWbemLocator")
If Err.Number then
  Wscript.Echo "Error 0x" & CStr(Hex(Err.Number)) & _
  " occurred in creating a locator object."
  If Err.Description <> "" Then
    Wscript.Echo "Error description: " & Err.Description & "."
  End If
  Err.Clear
  getRemoteConnect = False     'An error occurred
  Exit Function
End If

'Connect to the namespace which is either local or remote
Set objWMIService = objLocator.ConnectServer (strComputer, strNameSpace, strUserName, strPassword)
ObjWMIService.Security_.impersonationlevel = 3
If Err.Number Then
  Wscript.Echo "Error 0x" & CStr(Hex(Err.Number)) & " occurred in connecting to computer " _
  & strComputer & "."
  If Err.Description <> "" Then
    Wscript.Echo "Error description: " & Err.Description & "."
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
  Wscript.Echo "Error 0x" & CStr(Hex(Err.Number)) & _
  " occurred in attempting to retrieve the local computer name.  When"
  Wscript.Echo "this error occurs the process will continue to run against the local machine "
  Wscript.Echo "only and not a remote computer if it was specified."
  If Err.Description <> "" Then
    Wscript.Echo "Error description: " & Err.Description & "."
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
Private Function blnOpenFile(ByVal strFileName, _
                             ByRef objOpenFile)

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
  Wscript.Echo "Error 0x" & CStr(Hex(Err.Number)) & ": " & strIn
  If Err.Description <> "" Then
    Wscript.Echo "Error description: " & Err.Description
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
Wscript.Echo "Provides a list of drive letters and the corresponding drive type."
Wscript.Echo ""
Wscript.Echo "SYNTAX:"
Wscript.Echo "  DriveLetters.vbs"
Wscript.Echo "              [/C <computername>][/O <outputfile>][/Q <outputtype>"
Wscript.Echo "              [/U <username>][/W <password>]"
Wscript.Echo ""
Wscript.Echo ""
Wscript.Echo "PARAMETER SPECIFIERS:"
Wscript.Echo "   /A            List of available drive letters is provided."
Wscript.Echo ""
Wscript.Echo "   /S            Timestamp will be listed."
Wscript.Echo ""
Wscript.Echo "   computername  The name of the comptuer to check for available diskspace."
Wscript.Echo ""
Wscript.Echo "   username      The current user's name."
Wscript.Echo ""
Wscript.Echo "   password      Password of the current user."
Wscript.Echo ""
Wscript.Echo "   outputfile    The output file name."
Wscript.Echo ""
Wscript.Echo "   outputtype    One of Bare, Standard"
Wscript.Echo ""
Wscript.Echo "EXAMPLE:"
Wscript.Echo ""
Wscript.Echo "1. cscript DriveLetters.vbs /C MyMachine2 /Q Bare"
Wscript.Echo "      Check the computer MyMachine2 to and list it's drive information"
Wscript.Echo "      in an abbreviated format"
Wscript.Echo ""

End Sub
'********************************************************************
'*
'* Function GetDriveInformation()
'*
'* Purpose: Populates properties for the drives into arrays
'*
'* Input:   objWMIService       WMI object
'*
'* Output:  arrDriveLetters	    stores the drive letters
'*
'*          arrDriveType        store the 'type' of drive
'*
'********************************************************************
Private Function GetDriveInformation(objWMIService,	_
                                     ByRef arrDriveLetters, _
                                     ByRef arrDriveType)


ON ERROR RESUME NEXT

Dim objDisk
Dim colDisks
Dim strDiskFound
Dim strDeviceId
Dim	i                  'used for counters and loops



'Gets the logical disk drive information
i = 0 'Counter for the number of drives
Set colDisks = objWMIService.ExecQuery ("Select * from Win32_LogicalDisk")
For Each objDisk in colDisks
  Select Case objDisk.DriveType
    Case 1
      'No root directory
      ReDim Preserve arrDriveLetters(i)
      arrDriveLetters(i) = LCase(Left(objDisk.DeviceID, 1))
      ReDim Preserve arrDriveType(i)
      arrDriveType(i) = "No Root Directory"
      i= (i + 1)
    Case 2
      'Removable Disk
      ReDim Preserve arrDriveLetters(i)
      arrDriveLetters(i) = LCase(Left(objDisk.DeviceID, 1))
      ReDim Preserve arrDriveType(i)
      arrDriveType(i) = "Removable Disk"
      i= (i + 1)
    Case 3
      'Local Disk
      ReDim Preserve arrDriveLetters(i)
      arrDriveLetters(i) = LCase(Left(objDisk.DeviceID, 1))
      ReDim Preserve arrDriveType(i)
      arrDriveType(i) = "Local Disk"
      i= (i + 1)
    Case 4
      'Network Disk
      ReDim Preserve arrDriveLetters(i)
      arrDriveLetters(i) = LCase(Left(objDisk.DeviceID, 1))
      ReDim Preserve arrDriveType(i)
      arrIDriveType(i) = "Network Disk"
      i= (i + 1)
    Case 5
      'Compact Disk
      ReDim Preserve arrDriveLetters(i)
      arrDriveLetters(i) = LCase(Left(objDisk.DeviceID, 1))
      ReDim Preserve arrDriveType(i)
      arrDriveType(i) = "Compact Disk"
      i= (i + 1)
    Case 6
      'Ram Disk
      ReDim Preserve arrDriveLetters(i)
      arrDriveLetters(i) = LCase(Left(objDisk.DeviceID, 1))
      ReDim Preserve arrDriveType(i)
      arrDriveType(i) = "Ram Disk"
      i= (i + 1)
    Case Else
      ReDim Preserve arrDriveLetters(i)
      arrDriveLetters(i) = LCase(Left(objDisk.DeviceID, 1))
      ReDim Preserve arrDriveType(i)
      arrDriveType(i) = "Undetermined"
      i= (i + 1)
  End Select
Next

End Function
'********************************************************************
'*
'* Function GetUnusedDriveLetters
'*
'* Purpose: Creates an array of unsued drive letters
'*
'* Input:   arrDriveLetters               array of drive letters
'*          arrUnusedDriveLetters         empty array of unused drive
'*                                        letters
'*
'* Output:  arrUnusedDriveLetters         populated array of unsused
'*                                        drive letters
'*
'********************************************************************
Private Function GetUnusedDriveLetters(ByVal arrDriveLetters, _
                                       ByRef arrUnusedDriveLetters)

Dim i                            'counter for the alphabet array
Dim j                            'counter for the number of used drive letters
Dim k                            'counter for the unused drive letter array
Dim arrAlphabet(25)              'array of alphabetic characters
Dim strDriveLetterFound          'treated like a boolean, gets set to true or false

arrAlphabet(0)="a"
arrAlphabet(1)="b"
arrAlphabet(2)="c"
arrAlphabet(3)="d"
arrAlphabet(4)="e"
arrAlphabet(5)="f"
arrAlphabet(6)="g"
arrAlphabet(7)="h"
arrAlphabet(8)="i"
arrAlphabet(9)="j"
arrAlphabet(10)="k"
arrAlphabet(11)="l"
arrAlphabet(12)="m"
arrAlphabet(13)="n"
arrAlphabet(14)="o"
arrAlphabet(15)="p"
arrAlphabet(16)="q"
arrAlphabet(17)="r"
arrAlphabet(18)="s"
arrAlphabet(19)="t"
arrAlphabet(20)="u"
arrAlphabet(21)="v"
arrAlphabet(22)="w"
arrAlphabet(23)="x"
arrAlphabet(24)="y"
arrAlphabet(25)="z"

k = 0
For i = 0 To UBound(arrAlphabet)
  strDriveLetterFound = "False"
  j = 0
  Do While (strDriveLetterFound = "False") And (j <= UBound(arrDriveLetters))
    If LCase(arrAlphabet(i)) = LCase(arrDriveLetters(j)) Then
      strDriveLetterFound = "True"
    End If
    j = (j + 1)
  Loop
  If strDriveLetterFound = "False" Then
    ReDim Preserve arrUnusedDriveLetters(k)
    arrUnusedDriveLetters(k) = arrAlphabet(i)
    k = (k + 1)
  End If
Next

End Function
'********************************************************************
'*
'* Sub      WriteOutput()
'*
'* Purpose: Writes the output
'*
'* Input:   strOutputType                 type of output selected
'*          strTaskCommand                one of /list /check
'*          strTimeStamp                  timestamp option
'*          strOutputFile                 output file path
'*          strComputer                   computer name
'*          strUserName                   user name
'*          arrDriveLetters               array of used drive letters
'*          arrDriveType                  array of used drive types
'*          arrUnusedDriveLetters         array of unused drive letters
'*          objOutputFile                 file object
'*
'* Output:  Drive information
'*
'********************************************************************
Private Sub WriteOutput(strOutputType, _
                        strTaskCommand, _
                        strTimeStamp, _
                        strOutputFile, _
                        strComputer, _
                        strUserName, _
                        arrDriveLetters, _
                        arrDriveType, _
                        arrUnusedDriveLetters, _
                        objOutputFile)

ON ERROR RESUME NEXT


Dim i
Dim arrOutput()											'output array

If strOutputType = CONST_STANDARD_OUTPUT Then
  Call WriteStandardOutput(arrDriveLetters, _
                           arrDriveType, _
                           arrUnusedDriveLetters, _
                           strComputer, _
                           strUserName, _
                           strTaskCommand, _
                           strOutputType, _
                           arrOutput)
End If
If strOutputType = CONST_BARE_OUTPUT Then
  Call WriteBareOutput(arrDriveLetters, _
                       arrDriveType, _
                       arrUnusedDriveLetters, _
                       strTaskCommand, _
                       arrOutput)
End If

'Dispaly output
If strTimeStamp = CONST_TIMESTAMP_ON Then
  For i = 0 to UBound(arrOutput)
    WScript.echo (Now & Space(3) & arrOutput(i))
  Next
Else
  For i = 0 to UBound(arrOutput)
    WScript.echo arrOutput(i)
  Next
End If

'Write to file the same output
If Not IsEmpty(strOutputFile) Then
  If strTimeStamp = CONST_TIMESTAMP_ON Then
    For i = 0 to UBound(arrOutput)
      objOutputFile.WriteLine	(Now & Space(3) & arrOutput(i))
    Next
  Else
    For i = 0 to UBound(arrOutput)
      objOutputFile.WriteLine	(arrOutput(i))
    Next
  End If
  If Not IsEmpty(objOutputFile) Then
    objOutputFile.Close
    If Not strOutputType = CONST_BARE_OUTPUT Then
      Wscript.Echo "Results are saved in file " & strOutputFile & "."
    End If
  End If
End If


End Sub
'********************************************************************
'*
'* Function WriteStandardOutput
'*
'* Purpose: Builds the array arrOutput
'*
'*          arrDriveLetters                array of drive letters
'*          arrDriveType                   the 'type' of drive
'*          arrUnusedDriveLetters          array of unused drive letters
'*          strComputer                    name of the computer running against
'*          strUserName                    name of user used for authentication and authorization
'*          strTaskCommand                 one of used or unused for the drive letters
'*          strOutputType                  the type of output
'*
'* Output:  arrOutput                      array of all output lines
'*
'********************************************************************
Private Function WriteStandardOutput(ByVal arrDriveLetters, _
                                     ByVal arrDriveType, _
                                     ByVal arrUnusedDriveLetters, _
                                     ByVal strComputer, _
                                     ByVal strUserName, _
                                     ByVal strTaskCommand, _
                                     ByVal strOutputType, _
                                     ByRef arrOutput)


ON ERROR RESUME NEXT

Dim i
Dim j

i = 0
ReDim arrOutput(i)
arrOutput(i) = "DriveLetters.vbs Computer=" & strComputer & Space(1) & "User=" & strUserName

i = (i + 1)
ReDim Preserve arrOutput(i)
arrOutput(i) = "Output Type=" & strOutputType & Space(1) & "Task=" & strTaskCommand

i = (i + 1)
ReDim Preserve arrOutput(i)
arrOutput(i) = ""

i = (i + 1)
ReDim Preserve arrOutput(i)
Select Case strTaskCommand
  Case CONST_USED
    arrOutput(i) = "DRIVE LETTER" & Space(5) & "DRIVE TYPE"
    i = (i + 1)
    ReDim Preserve arrOutput(i + UBound(arrDriveLetters))
    For j = 0 to UBound(arrDriveLetters)
      arrOutput(i)=UCase(arrDriveLetters(j)) & Space(16) & arrDriveType(j)
      i = (i + 1)
    Next
  Case CONST_UNUSED
    arrOutput(i) = "DRIVE LETTER"
    i = (i + 1)
    ReDim Preserve arrOutput(i + UBound(arrUnusedDriveLetters))
    For j = 0 to UBound(arrUnusedDriveLetters)
      arrOutput(i)=UCase(arrUnusedDriveLetters(j))
      i = (i + 1)
    Next
End Select

End Function
'********************************************************************
'*
'* Function WriteBareOutput
'*
'* Purpose: Builds the array arrOutput
'*
'* Input:   arrDriveLetters         array of drive letters
'*          arrDriveType            type of drive
'*          arrUnusedDriveLetters   array of unused drive letters
'*          strTaskCommand          one of used or unused drive letters
'*
'* Output:  arrOutput               array of all output lines
'*
'********************************************************************
Private Function WriteBareOutput(ByVal arrDriveLetters, _
                                 ByVal arrDriveType, _
                                 ByVal arrUnusedDriveLetters, _
                                 ByVal strTaskCommand, _
                                 ByRef arrOutput)

ON ERROR RESUME NEXT

Dim strDriveFound
Dim i

Select Case strTaskCommand
  Case CONST_USED
    ReDim Preserve arrOutput(UBound(arrDriveLetters))
    For i = 0 to UBound(arrDriveLetters)
      arrOutput(i) = UCase(arrDriveLetters(i)) & Space(1) & arrDriveType(i)
    Next
  Case CONST_UNUSED
    ReDim Preserve arrOutput(UBound(arrUnusedDriveLetters))
    For i = 0 to UBound(arrUnusedDriveLetters)
      arrOutput(i) = UCase(arrUnusedDriveLetters(i))
    Next
End Select

End Function