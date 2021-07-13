'*************************************************************
'Send Email Messages
'*************************************************************
Dim Body, Subject, Attachment

'Create the Message Object
Set objMessage = CreateObject("CDO.Message") 

'Check how many parameters were passed in
If WScript.Arguments.Count = 0 Then
	'Message with NO passed Parameters
	'objMessage.Subject = "Test Message"
	'objMessage.TextBody = "This is some sample message text."
	'objMessage.To = "someemail@sabercorp.com"
ElseIf WScript.Arguments.Count < 4 Then
	'Message with passed Parameters
	objMessage.Subject = WScript.Arguments(0)
	objMessage.TextBody = WScript.Arguments(1)
	objMessage.To = WScript.Arguments(2)
Else
	'Message with passed Parameters
	objMessage.Subject = WScript.Arguments(0)
	objMessage.TextBody = WScript.Arguments(1)
	objMessage.To = WScript.Arguments(2)
	objMessage.AddAttachment WScript.Arguments(3)
End If

'Add Attachment
'objMessage.AddAttachment "C:\Test.txt"

'Sample test Subject
'objMessage.Subject = "Test Message" 

'Sample test Body
'objMessage.TextBody = "This is some sample message text."

'*************************************************************
'Edit the From Address to be who the Email is being sent from
'*************************************************************
objMessage.From = "SomeServer@SomeServer.com"

'*************************************************************
'Edit the To Address to be who the Email is being sent to
'*************************************************************
'objMessage.To = "stsfilenet@sabercorp.com;dublin_env@sabercorp.com"

'==This section provides the configuration information for the remote SMTP server.
'==Normally you will only change the server name or IP.
objMessage.Configuration.Fields.Item _
("http://schemas.microsoft.com/cdo/configuration/sendusing") = 2 

'************************************************************
'Name or IP of Remote SMTP Server
'************************************************************
'objMessage.Configuration.Fields.Item _
'("http://schemas.microsoft.com/cdo/configuration/smtpserver") = "smtpserver"

objMessage.Configuration.Fields.Item _
("http://schemas.microsoft.com/cdo/configuration/smtpserver") = "colex00.saber.root.sabercorp.com"


'Server port (typically 25)
objMessage.Configuration.Fields.Item _
("http://schemas.microsoft.com/cdo/configuration/smtpserverport") = 25 

objMessage.Configuration.Fields.Update

'==End remote SMTP server configuration section==

objMessage.Send
