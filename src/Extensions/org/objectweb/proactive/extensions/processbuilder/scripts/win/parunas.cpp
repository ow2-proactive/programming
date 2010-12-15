// parunas.cpp : Defines the entry point for the console application.
// Wrapper for process creation under a specific local user, creates
// a child process with a default environement, access rights to the 
// interactive windows station and desktop then waits until its finished.
// The following code is a slightly modified version of the official 
// msdn code "Starting an Interactive Client Process in C++"
// from http://msdn.microsoft.com/en-us/library/aa379608(v=VS.85).aspx
// Modifications:
// - Added command line options parsing
// - Added a verbose mode (prints [parunas]... to stderr)
// - Changed ZeroMemory to SecureZeroMemory
// - The removal of the ACEs is now done after the client process has finished
// - The return value of the wmain is the exit code of the child process
// - Removed inheritance of the parent environment
// - The child process working dir can be specified is the user directory as client user
// Requirements:
// - SE_RESTORE_NAME and SE_BACKUP_NAME privileges for LoadUserProfile
// - SE_ASSIGNPRIMARYTOKEN_NAME and SE_INCREASE_QUOTA_NAME privileges for CreateProcessAsUser

// Default include
#include "stdafx.h"
#include <windows.h>
#include <stdio.h>
// Required by CreateEnvironmentBlock
#include <UserEnv.h>

#define LOG_ERROR(...) if (bVerbose) wprintf(L"%s errno=%d\n", __VA_ARGS__, GetLastError());
#define LOG_INFO(...) if (bVerbose) wprintf(L"%s\n", __VA_ARGS__);

static BOOL bVerbose = false;

// Constants
#define DESKTOP_ALL (DESKTOP_READOBJECTS | DESKTOP_CREATEWINDOW | \
	DESKTOP_CREATEMENU | DESKTOP_HOOKCONTROL | DESKTOP_JOURNALRECORD | \
	DESKTOP_JOURNALPLAYBACK | DESKTOP_ENUMERATE | DESKTOP_WRITEOBJECTS | \
	DESKTOP_SWITCHDESKTOP | STANDARD_RIGHTS_REQUIRED)

#define WINSTA_ALL (WINSTA_ENUMDESKTOPS | WINSTA_READATTRIBUTES | \
	WINSTA_ACCESSCLIPBOARD | WINSTA_CREATEDESKTOP | \
	WINSTA_WRITEATTRIBUTES | WINSTA_ACCESSGLOBALATOMS | \
	WINSTA_EXITWINDOWS | WINSTA_ENUMERATE | WINSTA_READSCREEN | \
	STANDARD_RIGHTS_REQUIRED)

#define GENERIC_ACCESS (GENERIC_READ | GENERIC_WRITE | \
	GENERIC_EXECUTE | GENERIC_ALL)

// Prototypes
BOOL GetLogonSID(HANDLE hToken, PSID *ppsid);
VOID FreeLogonSID (PSID *ppsid);
int StartInteractiveClientProcess (
	WCHAR* username,       // client to log on
	WCHAR* domain,         // domain of client's account
	WCHAR* password,       // client's password
	WCHAR* commandLine,      // command line to execute
	WCHAR* workingDirectory  // working dir of the process
	);
BOOL AddAceToWindowStation(HWINSTA hwinsta, PSID psid);
BOOL AddAceToDesktop(HDESK hdesk, PSID psid);
BOOL RemoveAceFromWindowStation(HWINSTA hwinsta, PSID psid);
BOOL RemoveAceFromDesktop(HDESK hdesk, PSID psid);
BOOL isCurrentUserInAdminGroup();


void echo( bool on = true )
{
	DWORD  mode;
	HANDLE hConIn = GetStdHandle( STD_INPUT_HANDLE );
	GetConsoleMode( hConIn, &mode );
	mode = on
		? (mode |   ENABLE_ECHO_INPUT )
		: (mode & ~(ENABLE_ECHO_INPUT));
	SetConsoleMode( hConIn, mode );
}

int wmain(int argc, WCHAR **argv)
{	
	// The user is a valid account on the local computer
	WCHAR username[32] = L"";         // = L"tutu"; // Valid username
	WCHAR domain[32] = L"";           // = L".";    // Using local user account database
	WCHAR password[32] = L"";         // = L"tutu"; // Johnny's password
	WCHAR commandLine[32000] = L"";     // = L"C:\\Windows\\System32\\cmd.exe /c \"set & pause\"";
	WCHAR* workingDirectory = NULL;     // [1024] = L"C:\\Temp";
	BOOL bShowHelp = FALSE;	

	int arg;
	WCHAR *argn;
	WCHAR *argp;
	for (arg = 1; arg < argc && !bShowHelp; arg++) {

		if (argv[arg][0] != '-' && argv[arg][0] != '/') {
			break;
		}

		// Name
		argn = argv[arg]+1;
		// Parameter
		argp = argn;
		// Length
		int len = wcslen(argp);

		while (*argp && *argp != ':') {
			argp++;
		}

		if (*argp == ':') {
			*argp++ = '\0';
		}

		//printf("arg [%d]  : %ls\n", arg, argv[arg]);
		//printf("value     : %ls\n", argp);
		//printf("value len : %d\n", wcslen(argp));

		switch (argn[0]) {			
		case 'u': // The name of the user account under which to run the program
			if (len == 0) {				
				bShowHelp = TRUE;
			}
			swprintf_s(username, L"%ws", argp);
			break;
		case 'd': // Domain
			if (len == 0) {				
				bShowHelp = TRUE;
			}
			swprintf_s(domain, L"%ws", argp);
			break;
		case 'w': // Working directory
			if (len == 0) {
				printf("Please specify a value for the working directory option!");
				bShowHelp = TRUE;
			}
			workingDirectory = (wchar_t *) malloc(len);
			swprintf_s(workingDirectory, len, L"%ws", argp);
			break;
		case 'v': // Enable verbose mode
			bVerbose = true;
			break;
		case '?': // Help
			bShowHelp = TRUE;
			break;
		default:
			wprintf(L"Unknown argument: %ws\n", argv[arg]);
			bShowHelp = TRUE;
			break;
		}
	}

	// Check for username
	if (wcslen(username) == 0) {
		printf("No username!\n");
		bShowHelp = TRUE;
	} else {
		if (wcslen(domain) == 0) {
			printf("No domain!\n");
			bShowHelp = TRUE;
		}
	}

	if (bShowHelp) {
		printf(
			"Summary:\n"
			"    Allows a user to run programs with under different user.\n"
			"Usage:\n"
			"    parunas [options] <program>\n"
			"Options:\n"
			"    /u:<username>     -- The name of the user account under which to run the program. The password is read from stdin.\n"
			"    /d:<domain>       -- User domain of the account.\n"	
			"    /w:<dir>          -- The working directory.\n"
			"    /v                -- Enable verbose mode.\n" 
			"    /?                -- Display this help screen.\n"
			);
		return 1;
	}

	// Read the password from stdin
	echo(false);
	int result = wscanf_s(L"%ws", &password);	
	echo(true);
	if (result == 0) {
		printf("Unable to read password from stdin!\n %ws", password );
		return 1;
	}

	// The command line is the last argument
	WCHAR **w = CommandLineToArgvW(GetCommandLineW(), &argc);	
	swprintf_s(commandLine, L"%ws", w[argc-1]);	

	// Call StartInteractiveClientProcess()
	int exitCode = StartInteractiveClientProcess (
		username,      // client to log on
		domain,        // domain of client's account
		password,      // client's password
		commandLine,     // command line to execute
		workingDirectory // working directory
		);

	return exitCode;
}

BOOL GetLogonSID(HANDLE hToken, PSID *ppsid)
{
	BOOL bSuccess = FALSE;
	DWORD dwIndex;
	DWORD dwLength = 0;
	PTOKEN_GROUPS ptg = NULL;		

	// Verify the parameter passed in is not NULL
	if (ppsid == NULL) {
		LOG_ERROR("ppsid must not be NULL!");
		goto Cleanup;
	}

	// Get required buffer size and allocate the TOKEN_GROUPS buffer	
	LOG_INFO(L"Allocating buffer for TOKEN_GROUPS...");
	if (!GetTokenInformation(
		hToken,         // handle to the access token
		TokenGroups,    // get information about the token's groups
		(LPVOID) ptg,   // pointer to TOKEN_GROUPS buffer
		0,              // size of buffer
		&dwLength       // receives required buffer size
		))
	{
		if (GetLastError() != ERROR_INSUFFICIENT_BUFFER) {			
			LOG_ERROR(L"GetTokenInformation() 1 failed!");
			goto Cleanup;
		}

		ptg = (PTOKEN_GROUPS)HeapAlloc(GetProcessHeap(),HEAP_ZERO_MEMORY, dwLength);
		if (ptg == NULL) {
			LOG_ERROR(L"Unable to allocate heap for ptg!");
			goto Cleanup;
		}
	}

	// Get the token group information from the access token.
	if (!GetTokenInformation(
		hToken,         // handle to the access token
		TokenGroups,    // get information about the token's groups
		(LPVOID)ptg,    // pointer to TOKEN_GROUPS buffer
		dwLength,       // size of buffer
		&dwLength       // receives required buffer size
		))
	{
		LOG_ERROR(L"GetTokenInformation() 2 failed!");		
		goto Cleanup;
	}

	// wprintf(L"GetTokenInformation() is pretty fine!\n");
	// wprintf(L"ptg->GroupCount is %d\n", ptg->GroupCount);
	// wprintf(L"ptg->Groups->Attributes is %d\n", ptg->Groups->Attributes);	

	// Loop through the groups to find the logon SID.
	for (dwIndex = 0; dwIndex < ptg->GroupCount; dwIndex++)
		if ((ptg->Groups[dwIndex].Attributes & SE_GROUP_LOGON_ID) ==  SE_GROUP_LOGON_ID)
		{
			// Found the logon SID; make a copy of it.
			dwLength = GetLengthSid(ptg->Groups[dwIndex].Sid);
			*ppsid = (PSID) HeapAlloc(GetProcessHeap(),HEAP_ZERO_MEMORY, dwLength);

			LOG_INFO(L"Allocating heap for ppsid...");
			if (*ppsid == NULL) {
				LOG_ERROR(L"Unable to allocate heap for ppsid!");
				goto Cleanup;
			}

			LOG_INFO(L"Copying sid...");
			if (!CopySid(dwLength, *ppsid, ptg->Groups[dwIndex].Sid)) {				
				LOG_ERROR(L"Unable to copy sid!");
				HeapFree(GetProcessHeap(), 0, (LPVOID)*ppsid);
				goto Cleanup;
			}

			break;
		}
		bSuccess = TRUE;

Cleanup:

		// Free the buffer for the token groups.
		if (ptg != NULL)
			HeapFree(GetProcessHeap(), 0, (LPVOID)ptg);

		return bSuccess;
}

VOID FreeLogonSID (PSID *ppsid)
{	
	LOG_INFO(L"Freeing up the Logon SID...");
	HeapFree(GetProcessHeap(), 0, (LPVOID)*ppsid);
}

int StartInteractiveClientProcess(
	WCHAR* username,        // client to log on
	WCHAR* domain,          // domain of client's account
	WCHAR* password,        // client's password
	WCHAR* commandLine,       // command line to execute
	WCHAR* workingDirectory   // working directory
	)
{
	HANDLE hToken = NULL;
	HWINSTA hwinsta = NULL, hwinstaSave = NULL;
	PSID pSid = NULL;
	HDESK hdesk = NULL;
	LPVOID environment = NULL;
	STARTUPINFO si;
	PROFILEINFO pfi;
	DWORD dwResult = 0;
	DWORD cchPath = 1024;

	LOG_INFO(L"Logging the client...");
	// Log the client on to the local computer.
	if (!LogonUser(
		username,
		domain,
		password,
		LOGON32_LOGON_INTERACTIVE,
		LOGON32_PROVIDER_DEFAULT,&hToken)) {
			LOG_ERROR(L"LogonUser() failed!");
			goto Cleanup;
	}

	// Save a handle to the caller's current window station.
	LOG_INFO(L"Saving current window station...");
	if ((hwinstaSave = GetProcessWindowStation()) == NULL) {
		LOG_ERROR(L"GetProcessWindowStation() failed!");
		goto Cleanup;
	}

	// Get a handle to the interactive window station.
	LOG_INFO(L"Opening winsta0 window station...");
	hwinsta = OpenWindowStation(
		L"winsta0",                  // the interactive window station
		FALSE,                       // handle is not inheritable
		READ_CONTROL | WRITE_DAC);   // rights to read/write the DACL

	if (hwinsta == NULL) {
		LOG_ERROR(L"OpenWindowStation() failed!");
		goto Cleanup;
	}

	// To get the correct default desktop, set the caller's
	// window station to the interactive window station.
	LOG_INFO(L"Setting winsta0...");
	if ( !SetProcessWindowStation(hwinsta) ) {
		LOG_ERROR(L"SetProcessWindowStation() 1 failed!");
		goto Cleanup;
	}

	// Get a handle to the interactive desktop.
	LOG_INFO(L"Opening default interactive desktop...");
	hdesk = OpenDesktop(
		L"default",    // the interactive window station
		0,                  // no interaction with other desktop processes
		FALSE,         // handle is not inheritable
		READ_CONTROL | // request the rights to read and write the DACL
		WRITE_DAC | DESKTOP_WRITEOBJECTS | DESKTOP_READOBJECTS);

	// Restore the caller's window station.
	LOG_INFO(L"Restoring window station...");
	if ( !SetProcessWindowStation(hwinstaSave) ) {
		LOG_ERROR(L"SetProcessWindowStation() 2 failed!");
		goto Cleanup;
	}

	if (hdesk == NULL) {
		LOG_ERROR(L"OpenDesktop() failed!");
		goto Cleanup;	
	}

	// Get the SID for the client's logon session.
	LOG_INFO(L"Getting sid for client logon session...");
	if ( !GetLogonSID(hToken, &pSid) ) {
		LOG_ERROR(L"GetLogonSID() failed!");
		goto Cleanup;
	}

	// Allow logon SID full access to interactive window station.
	LOG_INFO(L"Allowing client sid full access to winsta0...");
	if ( !AddAceToWindowStation(hwinsta, pSid) ) {
		LOG_ERROR(L"AddAceToWindowStation() failed!");
		goto Cleanup;
	}

	// Allow logon SID full access to interactive desktop.
	LOG_INFO(L"Allowing client sid full access to desktop...");
	if ( !AddAceToDesktop(hdesk, pSid) ) {
		LOG_ERROR(L"AddAceToDesktop() failed!");
		goto Cleanup;
	}

	// Set up the PROFILEINFO structure to load the user profile
	SecureZeroMemory( &pfi, sizeof(PROFILEINFO) );
	pfi.dwSize = sizeof(PROFILEINFO);
	pfi.lpUserName = username;
	pfi.dwFlags = PI_NOUI; // Prevents the display of profile error messages

	BOOL isAdmin = isCurrentUserInAdminGroup();

	// Only admin can load the profile of an account
	LOG_INFO(L"Checking current user is in Administrator group...");
	if (isAdmin) {
		// Load the profile it requires SE_RESTORE_NAME and SE_BACKUP_NAME privileges for the calling process	
		LOG_INFO(L"Loading user profile...");
		if ( !LoadUserProfile(hToken, &pfi) ) {
			LOG_ERROR(L"LoadUserProfile() failed!");
			//goto Cleanup; // If it fails the environment will not contain username and USERPROFILE variables
		}
		LOG_INFO(L"check for working dir...");
		// If no working directory is specified		
		if (workingDirectory == NULL || wcslen(workingDirectory) == 0) {
			// Retrieve the user's profile directory	
			WCHAR tr[1024] = L"";
			LOG_INFO(L"Retrieve user profile directory...");
			if ( !GetUserProfileDirectory( hToken, tr, &cchPath ) ) {
				LOG_ERROR("GetProfilePath() failed!");		
				//goto Cleanup;
			} else { 
				workingDirectory = tr;
			}			
			LOG_INFO(workingDirectory);		
		}

		// Creates an environment that contains the variables of the client user
		if ( !CreateEnvironmentBlock(&environment, hToken, FALSE) ) {
			LOG_ERROR(L"CreateEnvironmentBlock() failed!");
			//goto Cleanup;
		}
	} else {
		LOG_INFO(L"Only Administrators can load a user profile ...");
	}

	// Initialize the STARTUPINFO structure.
	// Specify that the process runs in the interactive desktop.
	SecureZeroMemory(&si, sizeof(STARTUPINFO));
	si.cb = sizeof(STARTUPINFO);
	si.lpDesktop = L"winsta0\\default";

	// Impersonate client to ensure access to executable file.
	LOG_INFO(L"Impersonating client...");
	if ( !ImpersonateLoggedOnUser(hToken) ) {
		LOG_ERROR(L"ImpersonateLoggedOnUser() failed!");
		goto Cleanup;
	}

	PROCESS_INFORMATION pi;
	// Launch the process in the client's logon session.	
	LOG_INFO(L"Launching the process...");
	BOOL bResult = CreateProcessAsUser(
		hToken,            // client's access token
		NULL,              // file to execute
		commandLine,     // command line
		NULL,              // pointer to process SECURITY_ATTRIBUTES
		NULL,              // pointer to thread SECURITY_ATTRIBUTES
		TRUE,              // inherit system handles
		NORMAL_PRIORITY_CLASS | CREATE_UNICODE_ENVIRONMENT, //| CREATE_NO_WINDOW // creation flags
		environment,       // pointer to new environment block
		workingDirectory,// name of current directory
		&si,               // pointer to STARTUPINFO structure
		&pi                // receives information about new process
		);

	if( !bResult )
		LOG_ERROR(L"CreateProcessAsUser() failed!");

	// End impersonation of client
	LOG_INFO(L"Ending impersonation...");
	if( !RevertToSelf() )
		LOG_ERROR(L"RevertToSelf() failed!");

	if (isAdmin) {
		// Unload user profile
		LOG_INFO(L"Unloading user profile...");
		if( !UnloadUserProfile(hToken, pfi.hProfile) )
			LOG_ERROR(L"UnloadUserProfile() failed!");	

		// Destroy environment
		LOG_INFO(L"Destroying environment...");
		if( !DestroyEnvironmentBlock(environment) )
			LOG_ERROR(L"DestroyEnvironmentBlock() failed!");
	}

	// Close handles
	if (bResult) {
		if (pi.hProcess != INVALID_HANDLE_VALUE) {
			WaitForSingleObject(pi.hProcess, INFINITE);
		}

		if (!GetExitCodeProcess(pi.hProcess, &dwResult)) {
			LOG_ERROR(L"GetExitCodeProcess() failed!");			
		}

		if (pi.hProcess != INVALID_HANDLE_VALUE) {
			if(CloseHandle(pi.hProcess) == 0) 
				LOG_ERROR(L"Failed to close pi.hProcess handle!");
		}

		if (pi.hThread != INVALID_HANDLE_VALUE)	{
			if(CloseHandle(pi.hThread) == 0)
				LOG_ERROR(L"Failed to close pi.hThread handle!");
		}
	}

	// When running many times, it will keeps adding more ACLs to the Windows station until you hit some limit
	// http://support.microsoft.com/kb/185292/
	// Then SetUserObjectSecurity() will fails with ERROR_NOT_ENOUGH_QUOTA.
	// Undone any changes that were made to the Windows station and desktop.	
	LOG_INFO(L"Removing the ACE from Window station and desktop...");

	if ( !RemoveAceFromWindowStation(hwinsta, pSid) )
		LOG_ERROR(L"RemoveAceFromWindowStation() failed!");

	if ( !RemoveAceFromDesktop(hdesk, pSid) ) 
		LOG_ERROR(L"RemoveAceFromDesktop() failed!");	

Cleanup:

	if (hwinstaSave != NULL)
		SetProcessWindowStation (hwinstaSave);   
	// Free the buffer for the logon SID.
	if (pSid)
		FreeLogonSID(&pSid);   
	// Close the handles to the interactive window station and desktop.
	if (hwinsta)
		CloseWindowStation(hwinsta);
	if (hdesk)
		CloseDesktop(hdesk);   
	// Close the handle to the client's access token.
	if (hToken != INVALID_HANDLE_VALUE)
		CloseHandle(hToken);

	return dwResult;
}

/****** AddAceToWindowStation() ************************************************
*  NAME
*     AddAceToWindowStation() -- adds the ACE of the job user to the ACL of the 
*                                visible window station.
*
*  SYNOPSIS
*     static BOOL AddAceToWindowStation(HWINSTA hWinsta, PSID pSid)
*
*  FUNCTION
*    Adds the ACE (Access Control Entry) of the job user to the ACL
*    (Access Control List) of the visible window station.
*
*  INPUTS
*     HWINSTA hWinsta - Handle of the visible window station
*     PSID    pSid    - SID (Security Identifier) of the job user
*     
*  RESULT
*     BOOL - true if adding succeeded, false if it failed
*
*  NOTES
*******************************************************************************/
static BOOL AddAceToWindowStation(HWINSTA hWinsta, PSID pSid)
{
	ACCESS_ALLOWED_ACE   *pAce;
	ACL_SIZE_INFORMATION aclSizeInfo;
	BOOL                 bDaclExist;
	BOOL                 bDaclPresent;
	BOOL                 bRet = FALSE;
	DWORD                dwNewAclSize;
	DWORD                dwSidSize = 0;
	DWORD                dwSdSizeNeeded;
	PACL                 pAcl;
	PACL                 pNewAcl;
	PSECURITY_DESCRIPTOR pSd = NULL;
	PSECURITY_DESCRIPTOR pSdNew = NULL;
	PVOID                pTempAce;
	SECURITY_INFORMATION si = DACL_SECURITY_INFORMATION;
	unsigned int         i;

	//if(WaitForSingleObject(g_hWinstaACLMutex, INFINITE) == WAIT_OBJECT_0) {
	__try
	{
		// Obtain the DACL for the window station.
		if(!GetUserObjectSecurity(hWinsta, &si, pSd, dwSidSize, &dwSdSizeNeeded)) {
			if(GetLastError() == ERROR_INSUFFICIENT_BUFFER) {
				pSd = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(),
					HEAP_ZERO_MEMORY, dwSdSizeNeeded);
			}

			if (pSd == NULL) {
				__leave;
			}

			pSdNew = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(),
				HEAP_ZERO_MEMORY, dwSdSizeNeeded);

			if(pSdNew == NULL) {
				__leave;
			}

			dwSidSize = dwSdSizeNeeded;
			if(!GetUserObjectSecurity(hWinsta, &si, pSd, dwSidSize, &dwSdSizeNeeded)) {
				LOG_ERROR(L"GetUserObjectSecurity() failed!");
				__leave;
			}
		} else {
			__leave;
		}

		// Create a new DACL.
		if(!InitializeSecurityDescriptor(pSdNew, SECURITY_DESCRIPTOR_REVISION)) {
			LOG_ERROR(L"InitializeSecurityDescriptor() failed!");
			__leave;
		}

		// Get the DACL from the security descriptor.
		if(!GetSecurityDescriptorDacl(pSd, &bDaclPresent, &pAcl, &bDaclExist)) {
			LOG_ERROR(L"GetSecurityDescriptorDacl() failed!");
			__leave;
		}

		// Initialize the ACL.
		SecureZeroMemory(&aclSizeInfo, sizeof(ACL_SIZE_INFORMATION));
		aclSizeInfo.AclBytesInUse = sizeof(ACL);

		// Call only if the DACL is not NULL.
		if (pAcl != NULL) {
			// get the file ACL size info
			if(!GetAclInformation(pAcl, (LPVOID)&aclSizeInfo,
				sizeof(ACL_SIZE_INFORMATION), AclSizeInformation)) {
					__leave;
			}
		}

		if (bDaclPresent == TRUE) {
			// Check if object already has this ACL - if yes, don't set it again!
			if (aclSizeInfo.AceCount != 0) {
				for (i=0; i<aclSizeInfo.AceCount; i++) {
					// Get an ACE
					if (GetAce(pAcl, i, &pTempAce) != TRUE) {
						LOG_ERROR(L"GetAce() failed!");
						__leave;
					}
					if (EqualSid((PSID)&((ACCESS_ALLOWED_ACE*)pTempAce)->SidStart, pSid) == TRUE) {
						bRet = TRUE;					 
						__leave; // this SID already exists
					}
				}
			}
		}


		// Compute the size of the new ACL.
		dwNewAclSize = aclSizeInfo.AclBytesInUse 
			+ (2*sizeof(ACCESS_ALLOWED_ACE)) 
			+ (2*GetLengthSid(pSid)) - (2*sizeof(DWORD));

		// Allocate memory for the new ACL.
		pNewAcl = (PACL)HeapAlloc(GetProcessHeap(),  
			HEAP_ZERO_MEMORY, dwNewAclSize);

		if(pNewAcl == NULL) {
			__leave;
		}

		// Initialize the new DACL.
		if(!InitializeAcl(pNewAcl, dwNewAclSize, ACL_REVISION)) {
			LOG_ERROR(L"InitializeAcl() failed!");
			__leave;
		}

		// If DACL is present, copy it to a new DACL.
		if(bDaclPresent) {
			// Copy the ACEs to the new ACL.
			if(aclSizeInfo.AceCount) {
				for(i=0; i < aclSizeInfo.AceCount; i++) {
					if(!GetAce(pAcl, i, &pTempAce)) {
						LOG_ERROR(L"GetAce() failed!");
						__leave;
					}

					// Add the ACE to the new ACL.
					if(!AddAce(pNewAcl, ACL_REVISION, MAXDWORD,
						pTempAce, ((PACE_HEADER)pTempAce)->AceSize)) {
							LOG_ERROR(L"AddAce() failed!");
							__leave;
					}
				}
			}
		}

		// Add the first ACE to the window station.
		pAce = (ACCESS_ALLOWED_ACE *)HeapAlloc(
			GetProcessHeap(),
			HEAP_ZERO_MEMORY,
			sizeof(ACCESS_ALLOWED_ACE) + GetLengthSid(pSid) -
			sizeof(DWORD));

		if (pAce == NULL)
			__leave;

		pAce->Header.AceType  = ACCESS_ALLOWED_ACE_TYPE;
		pAce->Header.AceFlags = CONTAINER_INHERIT_ACE |
			INHERIT_ONLY_ACE | OBJECT_INHERIT_ACE;
		pAce->Header.AceSize  = (WORD)(sizeof(ACCESS_ALLOWED_ACE) +
			GetLengthSid(pSid) - sizeof(DWORD));
		pAce->Mask            = GENERIC_ACCESS;

		if (!CopySid(GetLengthSid(pSid), &pAce->SidStart, pSid)) {
			LOG_ERROR(L"CopySid() failed!");
			__leave;
		}

		if (!AddAce(
			pNewAcl,
			ACL_REVISION,
			MAXDWORD,
			(LPVOID)pAce,
			pAce->Header.AceSize)
			) {
				LOG_ERROR(L"AddAce() failed!");
				__leave;
		}

		// Add the second ACE to the window station.
		pAce->Header.AceFlags = NO_PROPAGATE_INHERIT_ACE;
		pAce->Mask            = WINSTA_ALL;

		if (!AddAce(
			pNewAcl,
			ACL_REVISION,
			MAXDWORD,
			(LPVOID)pAce,
			pAce->Header.AceSize)
			) {
				LOG_ERROR(L"AddAce() failed!");
				__leave;
		}

		// Set a new DACL for the security descriptor.
		if (!SetSecurityDescriptorDacl(
			pSdNew,
			TRUE,
			pNewAcl,
			FALSE)
			) {
				LOG_ERROR(L"SetSecurityDescriptorDacl() failed!");
				__leave;
		}

		// Set the new security descriptor for the window station.
		if(!SetUserObjectSecurity(hWinsta, &si, pSdNew)) {
			LOG_ERROR(L"SetUserObjectSecurity() failed!");
			__leave;
		}
		// Indicate success.
		bRet = TRUE;
	}
	__finally
	{
		// Free the allocated buffers.

		if (pAce != NULL)
			HeapFree(GetProcessHeap(), 0, (LPVOID)pAce);

		if (pNewAcl != NULL)
			HeapFree(GetProcessHeap(), 0, (LPVOID)pNewAcl);

		if (pSd != NULL)
			HeapFree(GetProcessHeap(), 0, (LPVOID)pSd);

		if (pSdNew != NULL)
			HeapFree(GetProcessHeap(), 0, (LPVOID)pSdNew);
	}
	//ReleaseMutex(g_hWinstaACLMutex);
	//}
	return bRet;
}

/****** AddAceToDesktop() ******************************************************
*  NAME
*     AddAceToDesktop() -- adds the ACE of the job user to the ACL of the 
*                          visible desktop.
*
*  SYNOPSIS
*     static BOOL AddAceToDesktop(HDESK hDesk, PSID pSid)
*
*  FUNCTION
*    Adds the ACE (Access Control Entry) of the job user to the ACL
*    (Access Control List) of the visible desktop.
*
*  INPUTS
*     HDESK hDesk - Handle of the visible desktop
*     PSID  pSid  - SID (Security Identifier) of the job user
*     
*  RESULT
*     BOOL - true if adding succeeded, false if it failed
*
*  NOTES
*******************************************************************************/
static BOOL AddAceToDesktop(HDESK hDesk, PSID pSid)
{
	ACL_SIZE_INFORMATION aclSizeInfo;
	BOOL                 bDaclExist;
	BOOL                 bDaclPresent;
	BOOL                 bRet      = FALSE;
	DWORD                dwSidSize = 0;
	DWORD                dwNewAclSize;
	DWORD                dwSdSizeNeeded;
	PVOID                pTempAce;
	PACL                 pAcl;
	PACL                 pNewAcl;
	PSECURITY_DESCRIPTOR pSd    = NULL;
	PSECURITY_DESCRIPTOR pSdNew = NULL;
	SECURITY_INFORMATION si     = DACL_SECURITY_INFORMATION;
	unsigned int         i;

	//if(WaitForSingleObject(g_hDeskACLMutex, INFINITE) == WAIT_OBJECT_0) {
	__try
	{
		// Obtain the security descriptor for the desktop object.
		if(!GetUserObjectSecurity(hDesk, &si, pSd, dwSidSize, &dwSdSizeNeeded)) {
			if(GetLastError() == ERROR_INSUFFICIENT_BUFFER) {
				pSd = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(),
					HEAP_ZERO_MEMORY, dwSdSizeNeeded);
				if(pSd == NULL) {
					__leave;
				}

				pSdNew = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(),
					HEAP_ZERO_MEMORY, dwSdSizeNeeded);
				if(pSdNew == NULL) {
					__leave;
				}

				dwSidSize = dwSdSizeNeeded;
				if(!GetUserObjectSecurity(hDesk, &si, pSd,
					dwSidSize, &dwSdSizeNeeded)) {
						__leave;
				}
			} else {
				__leave;
			}
		}

		// Create a new security descriptor.
		if(!InitializeSecurityDescriptor(pSdNew, SECURITY_DESCRIPTOR_REVISION)) {
			__leave;
		}

		// Obtain the DACL from the security descriptor.
		if (!GetSecurityDescriptorDacl(pSd, &bDaclPresent, &pAcl, &bDaclExist)) {
			__leave;
		}

		// Initialize.
		SecureZeroMemory(&aclSizeInfo, sizeof(ACL_SIZE_INFORMATION));
		aclSizeInfo.AclBytesInUse = sizeof(ACL);

		if(pAcl != NULL) {
			// Determine the size of the ACL information.
			if (!GetAclInformation(pAcl, (LPVOID)&aclSizeInfo,
				sizeof(ACL_SIZE_INFORMATION), AclSizeInformation)) {
					__leave;
			}
		}

		if (bDaclPresent) {
			// Checks if object already has this ACL - if yes, don't add it again!
			if (aclSizeInfo.AceCount) {
				for (i=0; i<aclSizeInfo.AceCount; i++) {
					// Get an ACE.
					if (!GetAce(pAcl, i, &pTempAce)) {
						__leave;
					}

					if (EqualSid((PSID)&((ACCESS_ALLOWED_ACE*)pTempAce)->SidStart, pSid)) {
						bRet = TRUE;
						__leave; // this SID already exists
					}
				}
			}
		}

		// Compute the size of the new ACL and allocate buffer
		dwNewAclSize = aclSizeInfo.AclBytesInUse
			+ sizeof(ACCESS_ALLOWED_ACE)
			+ GetLengthSid(pSid) - sizeof(DWORD);

		pNewAcl = (PACL)HeapAlloc(GetProcessHeap(), 
			HEAP_ZERO_MEMORY, dwNewAclSize);

		if(pNewAcl == NULL) {
			__leave;
		}

		if(!InitializeAcl(pNewAcl, dwNewAclSize, ACL_REVISION)) {
			__leave;
		}

		// If DACL is present, copy it to a new DACL.
		if(bDaclPresent) {
			// Copy the ACEs to the new ACL.
			if(aclSizeInfo.AceCount) {
				for(i=0; i < aclSizeInfo.AceCount; i++) {
					// Get an ACE.
					if(!GetAce(pAcl, i, &pTempAce)) {
						__leave;
					}

					// Add the ACE to the new ACL.
					if(!AddAce(pNewAcl, ACL_REVISION, MAXDWORD, pTempAce,
						((PACE_HEADER)pTempAce)->AceSize)) {
							__leave;
					}
				}
			}
		}

		// Add ACE to the DACL, set new DACL to the new security descriptor,
		// set new security descriptor for the desktop object.
		if(!AddAccessAllowedAce(pNewAcl, ACL_REVISION, DESKTOP_ALL, pSid)) {
			__leave;
		}
		if(!SetSecurityDescriptorDacl(pSdNew, TRUE, pNewAcl, FALSE)) {
			__leave;
		}
		if(!SetUserObjectSecurity(hDesk, &si, pSdNew)) {
			__leave;
		}
		bRet = TRUE;
	}
	__finally
	{
		// Free buffers.
		if(pNewAcl != NULL) {
			HeapFree(GetProcessHeap(), 0, (LPVOID)pNewAcl);
		}

		if(pSd != NULL) {
			HeapFree(GetProcessHeap(), 0, (LPVOID)pSd);
		}

		if(pSdNew != NULL) {
			HeapFree(GetProcessHeap(), 0, (LPVOID)pSdNew);
		}
	}
	//   ReleaseMutex(g_hDeskACLMutex);
	//}
	return bRet;
}


/****** RemoveAceFromWindowStation() ******************************************
*  NAME
*     RemoveAceFromWindowStation() -- removes the ACE of the job user from the
*                                     ACL of the visible window station.
*
*  SYNOPSIS
*     static BOOL RemoveAceFromWindowStation(HWINSTA hWinsta, PSID pSid)
*
*  FUNCTION
*    Removes the ACE (Access Control Entry) of the job user from the ACL
*    (Access Control List) of the visible window station.
*
*  INPUTS
*     HWINSTA hWinsta - Handle of the visible window station
*     PSID    pSid    - SID (Security Identifier) of the job user
*     
*  RESULT
*     BOOL - true if removing succeeded, false if it failed
*
*  NOTES
*******************************************************************************/
static BOOL RemoveAceFromWindowStation(HWINSTA hWinsta, PSID pSid)
{
	SECURITY_DESCRIPTOR  *pSD = NULL;
	BOOL                 bSecRet;
	BOOL                 bDaclPresent = TRUE;
	BOOL                 bDaclDefaulted = FALSE;
	DWORD                SDLength = 0;
	DWORD                SDLengthNeeded = 0;
	PACL                             pWinstaDacl;    
	LPVOID               pWinstaAce;
	SECURITY_INFORMATION si = DACL_SECURITY_INFORMATION;
	BOOL                 bRet = TRUE;
	BOOL                 bEqual;
	PSID                 pListSid;
	int                  nDeleted = 0;

	//if(WaitForSingleObject(g_hWinstaACLMutex, INFINITE) == WAIT_OBJECT_0) {
	__try
	{
		// Obtain DACL from Windows station, search for ACE, remove ACE from DACL
		bSecRet = GetUserObjectSecurity(hWinsta, &si, pSD, SDLength, &SDLengthNeeded);
		if(!bSecRet) {
			pSD = (SECURITY_DESCRIPTOR*)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, SDLengthNeeded);
		}
		bSecRet = GetUserObjectSecurity(hWinsta, &si, pSD, SDLengthNeeded, &SDLengthNeeded);
		bSecRet = GetSecurityDescriptorDacl(pSD, &bDaclPresent, &pWinstaDacl, &bDaclDefaulted);

		for(int i=pWinstaDacl->AceCount-1; i>=0; i--) {
			bSecRet = GetAce(pWinstaDacl, i, &pWinstaAce);
			if(((ACCESS_ALLOWED_ACE*)pWinstaAce)->Header.AceType == ACCESS_ALLOWED_ACE_TYPE) {
				pListSid = (PSID)&(((ACCESS_ALLOWED_ACE*)pWinstaAce)->SidStart);
				bEqual = TRUE;
				bSecRet = IsValidSid(pSid);
				bSecRet = IsValidSid(pListSid);
				DWORD dwSidLength = GetLengthSid(pSid);
				DWORD dwListSidLength = GetLengthSid(pListSid);

				for(DWORD j=0; j<dwSidLength && j<dwListSidLength; j++) {
					if(*((BYTE*)pListSid+j) != *((BYTE*)pSid+j)) {
						bEqual = FALSE;
						break;
					}
				}
				if(bEqual) {
					DeleteAce(pWinstaDacl, i);
					nDeleted++;
					if(nDeleted == 2) {
						break;
					}
				}
			}
		}
		SetUserObjectSecurity(hWinsta, &si, pSD);
	}

	__finally
	{
		if(pSD != NULL) {
			HeapFree(GetProcessHeap(), 0, (LPVOID)pSD);
		}
	}

	//   ReleaseMutex(g_hWinstaACLMutex);
	//}         
	return bRet;
}

/****** RemoveAceFromDesktop() ************************************************
*  NAME
*     RemoveAceFromDesktop() -- removes the ACE of the job user from the
*                               ACL of the visible desktop
*
*  SYNOPSIS
*     static BOOL RemoveAceFromDesktop(HDESK hDesk, PSID pSid)
*
*  FUNCTION
*    Removes the ACE (Access Control Entry) of the job user from the ACL
*    (Access Control List) of the visible desktop
*
*  INPUTS
*     HDESK hDesk - Handle of the visible desktop
*     PSID  pSid  - SID (Security Identifier) of the job user
*     
*  RESULT
*     BOOL - true if removing succeeded, false if it failed
*
*  NOTES
*******************************************************************************/
static BOOL RemoveAceFromDesktop(HDESK hDesk, PSID pSid)
{
	SECURITY_DESCRIPTOR  *pSD = NULL;
	BOOL                 bSecRet;
	BOOL                 bDaclPresent = TRUE;
	BOOL                 bDaclDefaulted = FALSE;
	DWORD                SDLength = 0;
	DWORD                SDLengthNeeded = 0;
	PACL                             pDeskDacl;    
	LPVOID               pDeskAce;
	SECURITY_INFORMATION si = DACL_SECURITY_INFORMATION;
	BOOL                 bRet = TRUE;
	BOOL                 bEqual;
	PSID                 pListSid;

	//if(WaitForSingleObject(g_hDeskACLMutex, INFINITE) == WAIT_OBJECT_0) {
	__try
	{
		// Obtain DACL from Windows station, search for ACE, remove ACE from DACL
		bSecRet = GetUserObjectSecurity(hDesk, &si, pSD, SDLength, &SDLengthNeeded);
		if(!bSecRet) {
			pSD = (SECURITY_DESCRIPTOR*)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, SDLengthNeeded);
		}
		bSecRet = GetUserObjectSecurity(hDesk, &si, pSD, SDLengthNeeded, &SDLengthNeeded);
		bSecRet = GetSecurityDescriptorDacl(pSD, &bDaclPresent, &pDeskDacl, &bDaclDefaulted);

		for(DWORD i=0; i<pDeskDacl->AceCount; i++) {
			bSecRet = GetAce(pDeskDacl, i, &pDeskAce);
			if(((ACCESS_ALLOWED_ACE*)pDeskAce)->Header.AceType == ACCESS_ALLOWED_ACE_TYPE) {
				pListSid = (PSID)&(((ACCESS_ALLOWED_ACE*)pDeskAce)->SidStart);
				bEqual = TRUE;
				bSecRet = IsValidSid(pSid);
				bSecRet = IsValidSid(pListSid);
				DWORD dwSidLength = GetLengthSid(pSid);
				DWORD dwListSidLength = GetLengthSid(pListSid);

				for(DWORD j=0; j<dwSidLength && j<dwListSidLength; j++) {
					if(*((BYTE*)pListSid+j) != *((BYTE*)pSid+j)) {
						bEqual = FALSE;
						break;
					}
				}
				if(bEqual) {
					DeleteAce(pDeskDacl, i);
					break;
				}
			}
		}
		SetUserObjectSecurity(hDesk, &si, pSD);
	}
	__finally
	{
		if(pSD != NULL) {
			HeapFree(GetProcessHeap(), 0, (LPVOID)pSD);
		}
	}
	//   ReleaseMutex(g_hDeskACLMutex);
	//}
	return bRet;
}

static BOOL isCurrentUserInAdminGroup(){
	SID_IDENTIFIER_AUTHORITY NtAuthority = SECURITY_NT_AUTHORITY;
	PSID AdministratorsGroup;
	// Initialize SID.
	if( !AllocateAndInitializeSid( &NtAuthority,
		2,
		SECURITY_BUILTIN_DOMAIN_RID,
		DOMAIN_ALIAS_RID_ADMINS,
		0, 0, 0, 0, 0, 0,
		&AdministratorsGroup))
	{
		// Initializing SID Failed.		
		return false;
	}
	// Check whether the token is present in admin group.
	BOOL IsInAdminGroup = FALSE;
	if( !CheckTokenMembership( NULL,
		AdministratorsGroup,
		&IsInAdminGroup ))
	{
		// Error occurred.
		IsInAdminGroup = FALSE;
	}

	// Free SID and return.
	FreeSid(AdministratorsGroup);
	return IsInAdminGroup;
}