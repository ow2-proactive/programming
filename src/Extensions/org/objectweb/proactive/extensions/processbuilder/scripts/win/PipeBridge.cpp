#include "stdafx.h"

#include <windows.h> 
#include <stdio.h> 
#include <conio.h>
#include <tchar.h>
#include <strsafe.h>
#include <process.h>

/**
* This C++ program serves for the following purpose:
*	It starts two names pipes (names are given as parameters) which will be 
*	open to write for any user. These are supposed to be output and error pipes 
*	to be used by the program (given as argument along with its parameters) 
*	spawned from the main method.
*	
*	The names of the two pipes have to be different. Also, passing the names of the 
*	pipes to the inner program is entirely the caller's task.
*
*/

/** 
*	HOW TO BUILD?
*
*	For the moment there is no elegant solution...
*	The simplest approach is to use Visual Studio (Visual C++) and import
*	this file into a new project (with precompiled headers). Than, it should
*	just compile nicely.
*/

#define BUFSIZE 1024

#define OSPL_E_PREFIX "_OS_PROCESS_LAUNCH_ERROR_"
#define OSPL_E_CAUSE "CAUSE"
#define OSLP_PACKAGE "org.objectweb.proactive.extensions.processbuilder.exception."

void strReplaceNewlines(CHAR* in, CHAR* out);

void DisplayError(CHAR* msg)
{
	printf("%s %sFatalProcessBuilderException %s [Windows] %s", OSPL_E_PREFIX, OSLP_PACKAGE, OSPL_E_CAUSE, msg); 
}

// Types

class SEC
{
    // Class to handle security attributes 
	//		- it will create an all-access security scenario for all logged in users
	//	Taken from MSDN tutorial
public:
    SEC();
    ~SEC();
    BOOL BuildSecurityAttributes( SECURITY_ATTRIBUTES* psa );
    
private:
    BOOL GetUserSid( PSID*  ppSidUser );

    BOOL    allocated;
    PSECURITY_DESCRIPTOR    psd;
    PACL    pACL;
    PTOKEN_USER pTokenUser;
};

/**  Constructor */ 
SEC::
    SEC() 
{ 
    allocated = FALSE; 
    psd = NULL; 
    pACL = NULL; 
    pTokenUser = NULL; 
}

/** Destructor */ 
SEC::
    ~SEC()
{ 
    if( allocated )
    {     
        if( psd ) HeapFree( GetProcessHeap(), 0, psd );
        if( pACL ) HeapFree( GetProcessHeap(), 0 , pACL );
        if( pTokenUser ) HeapFree( GetProcessHeap(), 0, pTokenUser );
        allocated = FALSE;
    }
}

/** Builds security attributes that allows read-only access to everyone
Input parameters: psa: security attributes to build
Output parameters: TRUE | FALSE */ 
BOOL SEC::
    BuildSecurityAttributes( SECURITY_ATTRIBUTES* psa )
{
    DWORD dwAclSize;
    PSID  pSidOthers = NULL; // Well-known AnonymousLogin SID
    PSID  pSidOwner = NULL;
    
    if( allocated ) return FALSE;

    SID_IDENTIFIER_AUTHORITY siaOthers = SECURITY_NT_AUTHORITY;
    SID_IDENTIFIER_AUTHORITY siaOwner = SECURITY_NT_AUTHORITY;
    
    do
    {
        psd = (PSECURITY_DESCRIPTOR) HeapAlloc( GetProcessHeap(),
                                                HEAP_ZERO_MEMORY,
                                                SECURITY_DESCRIPTOR_MIN_LENGTH);
        if( psd == NULL ) 
        {
            DisplayError( "HeapAlloc" );
            break;
        }

        if( !InitializeSecurityDescriptor( psd, SECURITY_DESCRIPTOR_REVISION) )
        {
            DisplayError( "InitializeSecurityDescriptor" );
            break;
        }

        // Build anonymous SID
        AllocateAndInitializeSid( &siaOthers, 1, 
								  SECURITY_AUTHENTICATED_USER_RID, 
                                  0,0,0,0,0,0,0,
                                  &pSidOthers
                                );

        if( !GetUserSid( &pSidOwner ) )
        {
            return FALSE;
        }

        // Compute size of ACL
        dwAclSize = sizeof(ACL) +
                    2 * ( sizeof(ACCESS_ALLOWED_ACE) - sizeof(DWORD) ) +
                    GetLengthSid( pSidOthers ) +
                    GetLengthSid( pSidOwner );
      
        pACL = (PACL)HeapAlloc( GetProcessHeap(), HEAP_ZERO_MEMORY, dwAclSize );
        if( pACL == NULL ) 
        {
            DisplayError( "HeapAlloc" );
            break;
        }
   
        InitializeAcl( pACL, dwAclSize, ACL_REVISION);
   
        
        if( !AddAccessAllowedAce( pACL,
                                  ACL_REVISION,
                                  GENERIC_ALL,
                                  pSidOwner
                                )) 
        {
            DisplayError( "AddAccessAllowedAce" );
            break;
        }
        
        //give all rights to logged in users
        if( !AddAccessAllowedAce( pACL,
                                  ACL_REVISION,
								  GENERIC_ALL, //GENERIC_READ | GENERIC_WRITE,
                                  pSidOthers
                                ) ) 
        {
            DisplayError( "AddAccessAllowedAce" );
            break;
        }
   
        if( !SetSecurityDescriptorDacl( psd, TRUE, pACL, FALSE) )
        {
            DisplayError( "SetSecurityDescriptorDacl" );
            break;
        }
      
        psa->nLength = sizeof(SECURITY_ATTRIBUTES);
        psa->bInheritHandle = TRUE;
        psa->lpSecurityDescriptor = psd;

        allocated = TRUE;
    }while(0);

	//removed these, as in server2008 they killed the app
	//there is some heap problem, but i have no idea :-s
	/*
    if( pSidOthers )   FreeSid( pSidOthers );
    if( pSidOwner )       FreeSid( pSidOwner );
    */
    if( !allocated )
    {
        if( psd ) HeapFree( GetProcessHeap(), 0, psd );
        if( pACL ) HeapFree( GetProcessHeap(), 0 , pACL );
    }

    return allocated;
}

/** Obtains the SID of the user running this thread or process.
Output parameters: ppSidUser: the SID of the current user,
TRUE   | FALSE: could not obtain the user SID */ 
BOOL SEC::
    GetUserSid( PSID*  ppSidUser )
{
    HANDLE      hToken;
    DWORD       dwLength;
    DWORD       cbName = 250;
    DWORD       cbDomainName = 250;
    
    if( !OpenThreadToken( GetCurrentThread(), TOKEN_QUERY, TRUE, &hToken) )
    {
        if( GetLastError() == ERROR_NO_TOKEN )
        {
            if( !OpenProcessToken( GetCurrentProcess(), TOKEN_QUERY, &hToken) )
            {
                return FALSE;
            }
        }
        else
        {
            return FALSE;
        }
    }


    if( !GetTokenInformation( hToken,       // handle of the access token
                              TokenUser,    // type of information to retrieve
                              pTokenUser,   // address of retrieved information 
                              0,            // size of the information buffer
                              &dwLength     // address of required buffer size
                              ))
    {
        if( GetLastError() == ERROR_INSUFFICIENT_BUFFER )
        {
            pTokenUser = (PTOKEN_USER) HeapAlloc( GetProcessHeap(), HEAP_ZERO_MEMORY, dwLength );
            if( pTokenUser == NULL )
            {
                return FALSE;
            }
        }
        else
        {
            return FALSE;
        }
    }

    if( !GetTokenInformation(   hToken,     // handle of the access token
                                TokenUser,  // type of information to retrieve
                                pTokenUser, // address of retrieved information 
                                dwLength,   // size of the information buffer
                                &dwLength   // address of required buffer size
                                ))
    {
        HeapFree( GetProcessHeap(), 0, pTokenUser );
        pTokenUser = NULL;

        return FALSE;
    }

    *ppSidUser = pTokenUser->User.Sid;
    return TRUE;
}


CHAR OSLP_EXIT[]="_OS_PROCESS_LAUNCHER_EXIT_CODE_";

// GLOBAL VARIABLE. Will be set in error thread, and will be read only after that one is dead.
//					This way there is no need for synchro.
DWORD exitCode = 0;

DWORD WINAPI StdOutThread(LPVOID); 
DWORD WINAPI StdErrThread(LPVOID); 

/**
* This is the main method of the program.
* It will start the 2 pipes, and their reading threads (one of them
* will write to stdout, and the other to stderr).	
* Then it will start the secondary program, and wait for its completion.
* After both reader threads exited, the main wil also exit (with the exit
* value returned by the inner application - see StdErrThread for explication).
**/
int _tmain(int argc, TCHAR *argv[]) 
{ 
	SEC sec;
    SECURITY_ATTRIBUTES     sa;
	
	sec.BuildSecurityAttributes( &sa );

	BOOL   oConnected = FALSE; 
	BOOL   eConnected = FALSE; 
	DWORD   dwThreadIdArray[2];
	HANDLE hThreadArray[3];
	HANDLE oPipe = INVALID_HANDLE_VALUE;
	HANDLE ePipe = INVALID_HANDLE_VALUE;

	TCHAR randomPipename[255];
	_wtmpnam_s(randomPipename);

	TCHAR outPipename[255];
	TCHAR errPipename[255];
	TCHAR savedPipename[256];
	wcscpy_s(outPipename, L"\\\\.\\pipe\0");
	wcscat_s(outPipename, randomPipename);
	wcscpy_s(savedPipename, outPipename);
	wcscpy_s(errPipename, outPipename);
	
	TCHAR outSuffix[3];
	wcscpy_s(outSuffix, L"o\0");
	wcscat_s(outPipename,outSuffix);
	TCHAR errSuffix[3];
	wcscpy_s(errSuffix, L"e\0");
	wcscat_s(errPipename,errSuffix);
	
	if (argc<2)
	{
		_tprintf(TEXT("Too few arguments.\nUsage: <program_to_exec> [<args...]"));
		exit(0);
	}

	oPipe = CreateNamedPipe( 
		outPipename,              // pipe name 
		//PIPE_ACCESS_DUPLEX,       
		PIPE_ACCESS_INBOUND,	  // read access 
		/*PIPE_TYPE_BYTE |
		PIPE_READMODE_BYTE |
		PIPE_WAIT,*/
		PIPE_TYPE_MESSAGE |	      // message type pipe 
		PIPE_READMODE_MESSAGE |   // message-read mode 
		PIPE_WAIT,                // blocking mode */
		PIPE_UNLIMITED_INSTANCES, // max. instances  
		0,                  // output buffer size 
		BUFSIZE,                  // input buffer size 
		0,                        // client time-out 
		&sa);                    // default security attribute 

	ePipe = CreateNamedPipe( 
		errPipename,              // pipe name 
		//PIPE_ACCESS_DUPLEX,       
		PIPE_ACCESS_INBOUND,	  // read access 
		/*PIPE_TYPE_BYTE |
		PIPE_READMODE_BYTE |
		PIPE_WAIT,*/
		PIPE_TYPE_MESSAGE |       // message type pipe 
		PIPE_READMODE_MESSAGE |   // message-read mode 
		PIPE_WAIT,                // blocking mode */
		PIPE_UNLIMITED_INSTANCES, // max. instances  
		0,                  // output buffer size 
		BUFSIZE,                  // input buffer size 
		0,                        // client time-out 
		&sa);                    // default security attribute 

	if (oPipe == INVALID_HANDLE_VALUE || ePipe == INVALID_HANDLE_VALUE) 
	{
		printf("%s %sFatalProcessBuilderException %s [Windows] Invalid pipe handle name!", OSPL_E_PREFIX, OSLP_PACKAGE, OSPL_E_CAUSE); 
		return -1;
	}


	hThreadArray[0] = CreateThread( 
		NULL,              // no security attribute 
		0,                 // default stack size 
		StdOutThread,    // thread proc
		(LPVOID) oPipe,    // thread parameter 
		0,                 // not suspended 
		&dwThreadIdArray[0]);      // returns thread ID 

	if (hThreadArray[0] == NULL) 
	{
		printf("%s %sFatalProcessBuilderException %s [Windows] Thread creation for stdout failed, GLE=%d.\n", OSPL_E_PREFIX, OSLP_PACKAGE, OSPL_E_CAUSE, GetLastError()); 
		return -1;
	}


	hThreadArray[1] = CreateThread( 
		NULL,              // no security attribute 
		0,                 // default stack size 
		StdErrThread,    // thread proc
		(LPVOID) ePipe,    // thread parameter 
		0,                 // not suspended 
		&dwThreadIdArray[1]);      // returns thread ID 
	if (hThreadArray[1] == NULL) 
	{
		printf("%s %sFatalProcessBuilderException %s [Windows] Thread creation for stderr failed, GLE=%d.\n", OSPL_E_PREFIX, OSLP_PACKAGE, OSPL_E_CAUSE, GetLastError()); 
		return -1;
	}

	//we start our application, and get the return value
	TCHAR progName[511];
	wcscpy_s(progName, argv[1]);
	wcscpy_s(progName, argv[1]);
	argv[0] = progName;
	argv[1] = savedPipename;
	intptr_t retv = _wspawnvp( _P_WAIT, argv[0], &argv[0] );

	if (retv == -1) {
		printf("%s java.lang.IOException %s ERRNO %d.\n", OSPL_E_PREFIX, OSPL_E_CAUSE, GetLastError()); 
		return -1;
	}

	WaitForMultipleObjects( 
		2,           // number of objects in array
		hThreadArray,     // array of objects
		TRUE,       // wait for all objects
		60000);       // 1 min timeout

	// The return value indicates which event is signaled
	CloseHandle(hThreadArray[0]); 
	CloseHandle(hThreadArray[1]); 

	return exitCode; 
} 

/**
* Thread that reads information on a pipe and writes it to stdout.
* Arguments:
*	lpvParam - HANDLE for pipe, whichis already opened
**/
DWORD WINAPI StdOutThread(LPVOID lpvParam)
{ 
	HANDLE hHeap      = GetProcessHeap();
	CHAR* pchMessage = (CHAR*)HeapAlloc(hHeap, 0, BUFSIZE*sizeof(CHAR));
	CHAR* pchMessageFixed = (CHAR*)HeapAlloc(hHeap, 0, BUFSIZE*sizeof(CHAR));
	DWORD	cbBufSize = BUFSIZE*sizeof(CHAR);

	DWORD cbBytesRead = 0;
	BOOL fSuccess = FALSE;
	BOOL fConnected = FALSE;
	HANDLE hPipe  = NULL;

	hPipe = (HANDLE) lpvParam; 

	fConnected = ConnectNamedPipe(hPipe, NULL) ? TRUE : (GetLastError() == ERROR_PIPE_CONNECTED); 
	if (fConnected)
	{
		while (1) 
		{ 
			// Read messages from the pipe. This simplistic code only allows messages
			// up to BUFSIZE characters in length.

			for (DWORD i=0; i<BUFSIZE; i++) pchMessage[i]='\0';
			for (DWORD i=0; i<BUFSIZE; i++) pchMessageFixed[i]='\0';

			fSuccess = ReadFile( 
				hPipe,        // handle to pipe 
				pchMessage,    // buffer to receive data 
				cbBufSize, // size of buffer 
				&cbBytesRead, // number of bytes read 
				NULL);        // not overlapped I/O 
			if (!fSuccess || cbBytesRead == 0)
			{   
				break;
			}

			if (strlen(pchMessage)>0) 
			{
				strReplaceNewlines(pchMessage, pchMessageFixed);
				fprintf(stdout,"%s", pchMessageFixed); 
				_flushall();
			}
		}

	}

	CloseHandle(hPipe);
	return fConnected;
}


int extractExitCode(CHAR* msg, int len, DWORD &exitv)
{
	int tokenLen = strlen(OSLP_EXIT);
	int i;

	for (i=0; i<tokenLen && i<len && CHAR(msg[i])==OSLP_EXIT[i]; i++);
	//the string starts with the exit signal
	if (i!=tokenLen) return 0;
	
	int retv = sscanf_s(msg+i,"%ld", &exitv);

	return retv;
}

/**
* Thread that reads information on a pipe and writes it to stderr.
* 
* ATTENTION: This method is also repsonsible for processing the exit code 
*			 of the writer process. This exit code (optional) will be sent
*			 through the pipe read by this thread in the following manner:
*			 "MARKER_FOR_EXIT EXIT_CODE", where the marker is OSPL_EXIT 
*			 (defined a bit further) and exit code is the exit value.
*			 If no such line appears on the pipe, the default exit value 
*			 of the main process will be 0.
*
*			 Remark: the marker-containing line does not appear on the
*			 output of this thread (i.e. stderr of this application)
*
* Arguments:
*	lpvParam - HANDLE for pipe, whichis already opened
**/
DWORD WINAPI StdErrThread(LPVOID lpvParam)
{ 
	HANDLE hHeap      = GetProcessHeap();
	CHAR* pchMessage = (CHAR*)HeapAlloc(hHeap, 0, BUFSIZE*sizeof(CHAR));
	CHAR* pchMessageFixed = (CHAR*)HeapAlloc(hHeap, 0, BUFSIZE*sizeof(CHAR));
	DWORD	cbBufSize = BUFSIZE*sizeof(CHAR);

	DWORD cbBytesRead = 0;

	BOOL fSuccess = FALSE;
	BOOL fConnected = FALSE;
	HANDLE hPipe  = NULL;

	hPipe = (HANDLE) lpvParam; 
	fConnected = ConnectNamedPipe(hPipe, NULL) ? TRUE : (GetLastError() == ERROR_PIPE_CONNECTED); 

	if (fConnected)
	{
		while (1) 
		{ 

			for (DWORD i=0; i<BUFSIZE; i++) pchMessage[i]='\0';
			for (DWORD i=0; i<BUFSIZE; i++) pchMessageFixed[i]='\0';

			// Read messages from the pipe. This simplistic code only allows messages
			// up to BUFSIZE characters in length.
			fSuccess = ReadFile( 
				hPipe,        // handle to pipe 
				pchMessage,    // buffer to receive data 
				cbBufSize, // size of buffer 
				&cbBytesRead, // number of bytes read 
				NULL);        // not overlapped I/O 
			
			if (!fSuccess || cbBytesRead == 0)
			{   
				break;
			}

			if (strlen(pchMessage)>0)
			{
				// Process the incoming message.
				if (!extractExitCode(pchMessage, BUFSIZE, exitCode))	
				{
					if (!(pchMessage[0]==10 && pchMessage[1]==0)) 
					{
						strReplaceNewlines(pchMessage, pchMessageFixed);
						fprintf(stderr,"%s", pchMessageFixed); 
						_flushall();
					}
				}
			}
		}

	}

	CloseHandle(hPipe);
	return fConnected;
}

void strReplaceNewlines(CHAR* in, CHAR* out) {
	DWORD i,j;
	DWORD len = strlen(in);
	DWORD offs = 0;
	for (i=0; i<len-offs; i++){
		if (in[i]=='\r' && in[i+1]=='\n') {
			offs++;
		}
		out[i]=in[i+offs];
	}
	out[i]='\0';
	
}