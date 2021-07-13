param(
	[Parameter(Mandatory=$True)] [string]$WebSiteUrl
)

# Build an extended CSharp WebClient class and expose the timeout setting
$Source = @"
	using System.Net;
 
	public class ExtendedWebClient : WebClient {
		public int Timeout;
 
		protected override WebRequest GetWebRequest(System.Uri address) {
			WebRequest request = base.GetWebRequest(address);
			if (request != null) {
				request.Timeout = Timeout;
			}
			return request;
		}
 
		public ExtendedWebClient() {
			Timeout = 100000; // the standard HTTP Request Timeout default
		}
	}
"@;

#Add the above WebClient class to this script
Add-Type -TypeDefinition $Source -Language CSharp  
	
$web = New-Object ExtendedWebClient
$web.Timeout = 180000 #3 minute time out

#Ignore any SSL trust issues
[Net.ServicePointManager]::ServerCertificateValidationCallback = {$true} 

$result = ""
"Getting website " + $WebSiteURL + " started ... Elapsed Time: "
$sw = [Diagnostics.Stopwatch]::StartNew()
try {
	$result = $web.DownloadString($WebSiteURL)
	$sw.stop()
	$sw.Elapsed
	if ($result.tolower().contains('successful')) {
		exit 0
	} else {
		$result
		exit 2
	}
} catch {
	$error[0].Exception
	exit 1
}
