/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.processbuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;

import org.objectweb.proactive.extensions.processbuilder.WindowsProcess.MyKernel32.PROCESSENTRY32;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;


/**
 * This class must be exclusively used by WindowsProcessBuilder.start() to
 * create new processes. It requires at least jna-3.3.0 see https://github.com/twall/jna.
 * This code is highly inspired by http://yajsw.sourceforge.net/
 * 
 * There are 2 ways to create processes on windows:
 * - Logon, Impersonate, CreateProcessAsUser, RevertToSelf: using this way the
 * created process does not breaks away from ProActiveAgent JobObject, but the 
 * drawback is that an extra call to CreateProcessWithLogon is needed to load the 
 * user env (impossible from a non-admin context). 
 * 
 * - CreateProcessWithLogon: the created process breaks away from upper JobObjects
 * and is assigned to a default JobObject of svchost.exe, the advantage is that the user
 * env is loaded automatically.
 * 
 * Sometimes the process exits with exit code -1073741502 (0xc0000142), it means that 
 * there is not enough non-interactive desktop heap.
 * For more information see http://support.microsoft.com/default.aspx?scid=kb;en-us;824422
 * and http://blogs.msdn.com/b/ntdebugging/archive/2007/01/04/desktop-heap-overview.aspx
 */
public final class WindowsProcess extends Process {

    static interface Options {
        final Map<String, Object> UNICODE_OPTIONS = new HashMap<String, Object>() {
            {
                put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
            }
        };
    }

    static interface MyAdvapi extends com.sun.jna.Library {
        final MyAdvapi INSTANCE = (MyAdvapi) Native.loadLibrary("Advapi32", MyAdvapi.class, Options.UNICODE_OPTIONS);

        /**
         * BOOL WINAPI CreateProcessWithLogonW( __in LPCWSTR lpUsername,
         * __in_opt LPCWSTR lpDomain, __in LPCWSTR lpPassword, __in DWORD
         * dwLogonFlags, __in_opt LPCWSTR lpApplicationName, __inout_opt LPWSTR
         * lpCommandLine, __in DWORD dwCreationFlags, __in_opt LPVOID
         * lpEnvironment, __in_opt LPCWSTR lpCurrentDirectory, __in
         * LPSTARTUPINFOW lpStartupInfo, __out LPPROCESS_INFORMATION
         * lpProcessInfo );
         */
        boolean CreateProcessWithLogonW(String lpUsername, String lpDomain, String lpPassword, int dwLogonFlags,
                String lpApplicationName, String lpCommandLine, int dwCreationFlags, String lpEnvironment,
                String lpCurrentDirectory, WinBase.STARTUPINFO lpStartupInfo,
                WinBase.PROCESS_INFORMATION lpProcessInfo);

        public static final int LOGON_WITH_PROFILE = 0x00000001;
    }

    static interface MyUserenv extends com.sun.jna.Library {
        final MyUserenv INSTANCE = (MyUserenv) Native.loadLibrary("Userenv", MyUserenv.class);

        /**
         * BOOL WINAPI GetUserProfileDirectory(
         *  __in       HANDLE hToken,
         *	__out_opt  LPTSTR lpProfileDir,
         * 	__inout    LPDWORD lpcchSize
         * );
         */
        boolean GetUserProfileDirectoryW(HANDLE hToken, Memory buff, IntByReference len);
    }

    /**
     * The Interface MyKernel32.
     */
    static interface MyKernel32 extends com.sun.jna.Library {
        final MyKernel32 INSTANCE = (MyKernel32) Native.loadLibrary("kernel32", MyKernel32.class);

        /** The PROCES s_ terminate */
        final int PROCESS_TERMINATE = 1;

        /** The T h32 c s_ snapprocess */
        final int TH32CS_SNAPPROCESS = 0x2;

        /**
         * HANDLE WINAPI CreateToolhelp32Snapshot( DWORD dwFlags, DWORD th32ProcessID );
         * 
         * @param dwFlags the dw flags
         * @param th32ProcessID the th32 process id
         * 
         * @return the pointer
         */
        HANDLE CreateToolhelp32Snapshot(int dwFlags, int th32ProcessID);

        /**
         * BOOL WINAPI Process32First( HANDLE hSnapshot, LPPROCESSENTRY32 lppe );
         * 
         * @param hSnapshot the h snapshot
         * @param lppe the lppe		
         * @return true, if successful
         */
        boolean Process32First(HANDLE hSnapshot, Structure lppe);

        /**
         * BOOL WINAPI Process32Next( HANDLE hSnapshot, LPPROCESSENTRY32 lppe );
         * 
         * @param hSnapshot the h snapshot
         * @param lppe the lppe
         * @return true, if successful
         */
        boolean Process32Next(HANDLE hSnapshot, Structure lppe);

        /**
         * typedef struct tagPROCESSENTRY32 { DWORD dwSize; DWORD cntUsage;
         * DWORD th32ProcessID; ULONG_PTR th32DefaultHeapID; DWORD th32ModuleID;
         * DWORD cntThreads; DWORD th32ParentProcessID; LONG pcPriClassBase;
         * DWORD dwFlags; TCHAR szExeFile[MAX_PATH]; } PROCESSENTRY32,
         * PPROCESSENTRY32;
         * The Class PROCESSENTRY32.
         */
        public static final class PROCESSENTRY32 extends Structure {
            /** The dw size */
            public int dwSize;

            /** The cnt usage */
            public int cntUsage;

            /** The th32 process id */
            public int th32ProcessID;

            /** The th32 default heap id */
            public ULONG_PTR th32DefaultHeapID;

            /** The th32 module id */
            public int th32ModuleID;

            /** The cnt threads */
            public int cntThreads;

            /** The th32 parent process id */
            public int th32ParentProcessID;

            /** The pc pri class base */
            public NativeLong pcPriClassBase;

            /** The dw flags */
            public int dwFlags;

            /** The sz exe file */
            public char[] szExeFile;

            private static List<String> fields;

            static {
                fields = new ArrayList<>(10);
                fields.add("dwSize");
                fields.add("cntUsage");
                fields.add("th32ProcessID");
                fields.add("th32DefaultHeapID");
                fields.add("th32ModuleID");
                fields.add("cntThreads");
                fields.add("th32ParentProcessID");
                fields.add("pcPriClassBase");
                fields.add("dwFlags");
                fields.add("szExeFile");
            }

            @Override
            protected List getFieldOrder() {
                return fields;
            }
        }
    }

    /** User information needed for the security context of the process */
    private final String domain, user, password;

    /** The read/write handles of in/out/err streams */
    private final HANDLEByReference inRead, inWrite, outRead, outWrite, errRead, errWrite;

    /** The java file descriptors for in/out/err streams */
    private final FileDescriptor in_fd, out_fd, err_fd;

    /** The process input stream */
    private InputStream inputStream;

    /** The process output stream. */
    private OutputStream outputStream;

    /** The process error stream */
    private InputStream errorStream;

    /** The PID of the process, default values is -1 */
    private int pid;

    /** Is this process destroyed */
    private boolean destroyed;

    /** The handle to the native process */
    private HANDLE handle;

    /**
     * Creates a new instance of the process.
     * 
     * @param domain the account domain 
     * @param username the account username 
     * @param password the account password
     */
    public WindowsProcess(final String domain, final String username, final String password) {
        this.domain = domain;
        this.user = username;
        this.password = password;

        this.inRead = new HANDLEByReference();
        this.inWrite = new HANDLEByReference();
        this.outRead = new HANDLEByReference();
        this.outWrite = new HANDLEByReference();
        this.errRead = new HANDLEByReference();
        this.errWrite = new HANDLEByReference();

        this.in_fd = new FileDescriptor();
        this.out_fd = new FileDescriptor();
        this.err_fd = new FileDescriptor();

        this.pid = -1;
    }

    /**
     * Start a new process under the specified user.
     * 
     * @param command the command to execute
     * @throws IOException in case of error
     */
    public void start(final String command) throws IOException, OSUserException, FileNotFoundException {
        this.start(command, null, null);
    }

    /**
     * Start a new process under the specified user.
     * 
     * @param command the command to execute
     * @param env the env of the process
     * @param path the working dir of the process
     * @throws IOException in case of error
     */
    public void start(final String command, final Map<String, String> env, final String path)
            throws IOException, OSUserException, FileNotFoundException {
        final StringTokenizer st = new StringTokenizer(command);
        final String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();

        this.start(cmdarray, env, path);
    }

    /** 
     * Starts a new process under the specified user.
     * <p>
     * The default priority is normal. 
     * 
     * @return <tt>true</tt> if the process has started successfully, <tt>false</tt> if the process is already running 
     */
    public void start(final String cmd[], final Map<String, String> environment, // can be null
            final String path // can be null 
    ) throws IOException, OSUserException, FileNotFoundException {

        // Merge the command array into a single string
        final String lpCommandLine = this.internalMergeCommand(cmd);

        // The handle must be always closed
        final HANDLEByReference hTokenRef = new HANDLEByReference();

        try {
            /////////////////////////////////////////////////////////////////////////////////////
            // The client subprocess used not to work with CreateProcessWithLogonW since it
            // broke away from ProActive Agent job object. Instead it was needed to use
            // LogonUser, Impersonate, CreateProcessAsUser, Revert ...
            // On recent windows version though, it is the opposite, CreateProcessAsUser always fails
            // with a 1314 error and CreateProcessWithLogonW works, even when executing with a ProActive
            // agent. It may break when the agent is configured to run as the system account.
            /////////////////////////////////////////////////////////////////////////////////////							

            if (!Advapi32.INSTANCE.LogonUser(this.user,
                                             this.domain,
                                             this.password,
                                             WinBase.LOGON32_LOGON_INTERACTIVE,
                                             WinBase.LOGON32_PROVIDER_DEFAULT,
                                             hTokenRef)) {

                // The logon failure must result as a OSUserException
                final int err = Kernel32.INSTANCE.GetLastError();
                final String mess = "LogonUser error=" + err + ", " + Kernel32Util.formatMessageFromLastErrorCode(err);
                throw new OSUserException(mess);
            }

            /////////////////////////////////////////////////////////////////////////////////////
            // To get the user env from a non-admin context (LoadUserProfile requires to be admin),
            // the user env is read from the stdout of the cmd.exe process created using
            // CreateProcessWithLogonW, this works from a non-admin context.
            /////////////////////////////////////////////////////////////////////////////////////

            Map<String, String> resEnv = environment;
            // If not defined create get a new ProcessEnvironment from the java ProcessBuilder   			
            if (resEnv == null) {
                ProcessBuilder b = new ProcessBuilder();
                resEnv = b.environment();
                resEnv.clear();
            }

            String lpEnvironment = null;
            try {
                // Dump user env
                lpEnvironment = this.internalGetUserEnv(resEnv);
            } catch (Exception e) {
                // If the env dump has failed the user profile path cannot be found 
                e.printStackTrace();
            }

            // try to get the USERPROFILE from dumped env
            String lpPath = (path == null ? resEnv.get("USERPROFILE") : path);
            if (lpPath == null) {
                // Use another way to get the user profile 
                IntByReference cbSid = new IntByReference(WinDef.MAX_PATH);
                Memory m = new Memory(cbSid.getValue());

                boolean success = MyUserenv.INSTANCE.GetUserProfileDirectoryW(hTokenRef.getValue(), m, cbSid);
                if (!success) {
                    // No way to get the user profile, we cannot let the process start in C:\Windows
                    win32ErrorIOException("GetUserProfileDirectoryW");
                }
                lpPath = m.getString(0, true);
            }

            /////////////////////////////////////////////////////////////////////////////////////
            // If the environment is NULL, the new process uses the environment of the calling
            // process. An env block consists of a null-terminated block of null-terminated strings.
            // Each string is in the following form: name=value\0 
            /////////////////////////////////////////////////////////////////////////////////////

            // Call the startWithLogon method which uses CreateProcessWithLogonW to start the process
            startWithLogon(cmd, lpEnvironment, lpPath);

        } catch (Exception ex) {

            // If RuntimeException re-throw the internal IOException 
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else if (ex instanceof OSUserException) {
                throw (OSUserException) ex;
            } else {
                throw new IOException(ex);
            }
        } finally {

            // Always clean up the user token handle
            closeSafely(hTokenRef);
        }
    }

    public void startWithLogon(final String cmd[], String lpEnvironment, String lpPath) throws IOException {
        // Merge the command array into a single string
        final String lpCommandLine = this.internalMergeCommand(cmd);

        try {

            // Fill the security attributes
            final WinBase.SECURITY_ATTRIBUTES sa = new WinBase.SECURITY_ATTRIBUTES();
            sa.lpSecurityDescriptor = null;
            sa.bInheritHandle = true;// true otherwise streams are not piped
            sa.write();

            // Create pipes
            if (!(Kernel32.INSTANCE.CreatePipe(this.inRead, this.inWrite, sa, 0) &&
                  Kernel32.INSTANCE.CreatePipe(this.outRead, this.outWrite, sa, 0) &&
                  Kernel32.INSTANCE.CreatePipe(this.errRead, this.errWrite, sa, 0))) {
                throw win32ErrorIOException("CreatePipe");
            }

            final WinBase.STARTUPINFO si = new WinBase.STARTUPINFO();
            si.dwFlags = WinBase.STARTF_USESHOWWINDOW | WinBase.STARTF_USESTDHANDLES;
            si.hStdInput = this.inRead.getValue();
            si.hStdOutput = this.outWrite.getValue();
            si.hStdError = this.errWrite.getValue();
            si.wShowWindow = new WinDef.WORD(0); // SW_HIDE
            si.write();

            final WinBase.PROCESS_INFORMATION pi = new WinBase.PROCESS_INFORMATION();

            /////////////////////////////////////////////////////////////////////////////////////
            // CreateProcessWithLogonW used not to work on old windows systems because its parent process is svchost.exe and
            // this broke away from ProActive Agent job object.
            // On recent windows, the problem seems to have disappeared.
            /////////////////////////////////////////////////////////////////////////////////////

            boolean result = MyAdvapi.INSTANCE.CreateProcessWithLogonW(/* String */this.user,
                                                                       this.domain,
                                                                       this.password,
                                                                       /* int */MyAdvapi.LOGON_WITH_PROFILE, // load user profile
                                                                       /* String */null, // The name of the module to be executed
                                                                       /* String */lpCommandLine, // The command line to be executed 
                                                                       /* int */WinBase.CREATE_NO_WINDOW |
                                                                                                  WinBase.CREATE_UNICODE_ENVIRONMENT, // creation flags
                                                                       /* String */lpEnvironment, // the new process uses an environment created from the profile of the user
                                                                       /* String */lpPath, // the new process has the same current drive and directory as the calling process
                                                                       /* WinBase.STARTUPINFO */si, // pointer to STARTUPINFO or STARTUPINFOEX structure
                                                                       /*
                                                                        * WinBase.
                                                                        * PROCESS_INFORMATION
                                                                        */pi); // pointer to PROCESS_FORMATION structure

            if (!result) {
                final int CreateProcessWithLogonWError = Kernel32.INSTANCE.GetLastError();
                final String messageFromLastErrorCode = Kernel32Util.formatMessageFromLastErrorCode(CreateProcessWithLogonWError);
                throw new IOException("CreateProcessWithLogonW error=" + CreateProcessWithLogonWError + ", " +
                                      messageFromLastErrorCode + " [lpPath=" + lpPath + ", lpCommandLine=" +
                                      lpCommandLine + "]");
            }

            Kernel32.INSTANCE.CloseHandle(pi.hThread);
            this.pid = pi.dwProcessId.intValue();
            this.handle = pi.hProcess;

            // Connect java-side streams
            this.internalConnectStreams();
        } catch (Exception ex) {
            // Clean up the parent's side of the pipes in case of failure only
            closeSafely(this.inWrite);
            closeSafely(this.outRead);
            closeSafely(this.errRead);

            // If rethrow the internal IOException 
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        } finally {
            // Always clean up the child's side of the pipes
            closeSafely(this.inRead);
            closeSafely(this.outWrite);
            closeSafely(this.errWrite);
        }
    }

    private void internalConnectStreams() {
        // Update handles referenced java file descriptors
        writefd(this.in_fd, this.inWrite.getValue());
        writefd(this.out_fd, this.outRead.getValue());
        writefd(this.err_fd, this.errRead.getValue());

        // Connect to java side streams
        this.outputStream = new BufferedOutputStream(new FileOutputStream(this.in_fd));
        this.inputStream = new BufferedInputStream(new FileInputStream(this.out_fd));
        this.errorStream = new BufferedInputStream(new FileInputStream(this.err_fd));
    }

    private String internalMergeCommand(final String[] cmd) {
        // Same a java.lang.ProcessImpl() Win32 CreateProcess requires cmd[0] to be normalized
        cmd[0] = new File(cmd[0]).getPath();

        final StringBuilder cmdbuf = new StringBuilder(80);
        for (int i = 0; i < cmd.length; i++) {
            if (i > 0) {
                cmdbuf.append(' ');
            }
            String s = cmd[i];
            if (s.indexOf(' ') >= 0 || s.indexOf('\t') >= 0) {
                if (s.charAt(0) != '"') {
                    cmdbuf.append('"');
                    cmdbuf.append(s);
                    if (s.endsWith("\\")) {
                        cmdbuf.append("\\");
                    }
                    cmdbuf.append('"');
                } else if (s.endsWith("\"")) {
                    /* The argument has already been quoted. */
                    cmdbuf.append(s);
                } else {
                    /* Unmatched quote for the argument. */
                    throw new IllegalArgumentException();
                }
            } else {
                cmdbuf.append(s);
            }
        }
        return cmdbuf.toString();
    }

    /** overrideEnv must be an instance of java.lang.ProcessEnvironment */
    private String internalGetUserEnv(final Map<String, String> overrideEnv // never null
    ) throws IOException, InterruptedException {

        WindowsProcess envProcess = new WindowsProcess(this.domain, this.user, this.password);
        try {
            envProcess.startWithLogon(new String[] { "cmd.exe", "/c", "set" }, null, null);

            int exitCode = envProcess.waitFor();
            if (exitCode != 0) {
                throw win32ErrorIOException("Unable to read user environment from cmd.exe, exitCode=" + exitCode);
            }

            // Read stdout that contains the user environment
            final InputStreamReader isr = new InputStreamReader(envProcess.inputStream);
            final BufferedReader br = new BufferedReader(isr);

            // Fill the env map with values read from the stdout of the set command
            try {
                String[] vals;
                String line, varname, value;
                while ((line = br.readLine()) != null) {
                    vals = line.split("=");
                    varname = vals[0];
                    value = overrideEnv.get(varname);
                    if (value == null) {
                        overrideEnv.put(varname, vals[1]);
                    }
                }
            } catch (IOException e) {
                throw e;
            } finally {
                try {
                    br.close();
                } catch (Exception e) {
                }

                try {
                    isr.close();
                } catch (Exception e) {
                }
            }

            // Use reflection to invoke the static method 
            // java.lang.ProcessEnvironment.toEnvironmentBlock(Map<String,String> map)
            // that returns a String
            String block = null;
            try {
                Class<?> cl = Class.forName("java.lang.ProcessEnvironment");
                for (final Method method : cl.getDeclaredMethods()) {
                    if (isToEnvironmentBlockMethodWithMapParameter(method)) {
                        method.setAccessible(true);
                        block = (String) method.invoke(null, overrideEnv);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return block;
        } finally {
            // Always destroy, close the process
            envProcess.destroy();
            envProcess.close();
        }
    }

    private boolean isToEnvironmentBlockMethodWithMapParameter(Method method) {
        return "toEnvironmentBlock".equals(method.getName()) && (method.getParameterTypes().length == 1) &&
               (method.getParameterTypes()[0].equals(Map.class));
    }

    /**
     * Causes the current thread to wait, if necessary, until the 
     * process represented by this object has terminated.
     * 
     * @return     the exit value of the process. By convention, 
     *             <code>0</code> indicates normal termination.
     * @exception  InterruptedException  if the current thread is 
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    @Override
    public int waitFor() throws InterruptedException {
        while (true) {
            this.internalWaitFor(1000);
            try {
                return this.exitValue();
            } catch (IllegalThreadStateException e) {
                // The process has not exited
                continue;
            }
        }
    }

    /**
     * This method returns immediately if the subprocess has already terminated.
     * If the subprocess has not yet terminated, the calling thread will be
     * blocked until the subprocess exits with the specified timeout.
     *
     * @return     the exit value of the process. By convention, 
     *             <code>0</code> indicates normal termination.
     * @exception  InterruptedException  if the current thread is 
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    public int waitFor(long timeoutInMillis) throws InterruptedException, TimeoutException {
        if (timeoutInMillis > Integer.MAX_VALUE) {
            timeoutInMillis = Integer.MAX_VALUE;
        }

        // Since there is no way to wait for "interruptible"
        // the thread is checked for interruption every 1000 milliseconds		
        final int periodicTimeout = (timeoutInMillis < 1000 ? (int) timeoutInMillis : 1000);
        int cumulatedTimeout = periodicTimeout;

        do {
            this.internalWaitFor(periodicTimeout);
            cumulatedTimeout += periodicTimeout;
        } while (cumulatedTimeout < timeoutInMillis);

        try {
            return this.exitValue();
        } catch (IllegalThreadStateException e) {
            throw new TimeoutException("The timeout has expired");
        }
    }

    private void internalWaitFor(final int timeoutInMillis) throws InterruptedException {
        final int res = Kernel32.INSTANCE.WaitForSingleObject(this.handle, timeoutInMillis);
        if (res < 0 || res == WinBase.WAIT_FAILED) {
            win32ErrorRuntime("WaitForSingleObject");
        }

        // Check for interruption
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * Returns the exit value for the subprocess.
     *
     * @return  the exit value of the subprocess represented by this 
     *          <code>WindowsProcess</code> object. by convention, the value 
     *          <code>0</code> indicates normal termination.
     * @exception  IllegalThreadStateException  if the subprocess represented 
     *             by this <code>WindowsProcess</code> object has not yet terminated.
     */
    @Override
    public int exitValue() {
        final IntByReference exitCodeRef = new IntByReference();
        // Retrieves the termination status of the specified process		
        boolean success = Kernel32.INSTANCE.GetExitCodeProcess(this.handle, exitCodeRef);
        if (!success) {
            win32ErrorRuntime("GetExitCodeProcess");
        }
        // As said in msdn an application should not use STILL_ACTIVE (259) as an error code	
        int exitCode = exitCodeRef.getValue();
        if (exitCode == WinBase.STILL_ACTIVE) {
            throw new IllegalThreadStateException("process has not exited");
        }
        return exitCode;
    }

    /**
     * To know if the process is running.
     * 
     * @return <tt>false</tt> if not yet started
     */
    public boolean isRunning() {
        if (this.pid <= 0) {
            return false;
        }
        try {
            this.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            // If exception the then it's still active
            return true;
        }
    }

    /**
     * Kills the subprocess. The subprocess represented by this object is
     * forcibly terminated.
     */
    @Override
    public void destroy() {
        if (this.destroyed) {
            return;
        }
        try {
            // Terminate the child processes and this process with exit code 1
            // as it's done by java.lang.Process#destroy();
            if (this.isRunning()) {
                List<Integer> tree = getProcessTree(this.pid);
                int retry = 0;
                while (tree.size() < 1 && retry++ < 20) {
                    tree = getProcessTree(this.pid);
                }

                // Killing a process must be done from an impersonated context
                final HANDLEByReference phUser = new HANDLEByReference();

                if (!Advapi32.INSTANCE.LogonUser(this.user,
                                                 this.domain,
                                                 this.password,
                                                 WinBase.LOGON32_LOGON_INTERACTIVE,
                                                 WinBase.LOGON32_PROVIDER_DEFAULT,
                                                 phUser)) {
                    win32ErrorRuntime("LogonUser");
                }

                // Impersonate current user
                if (!Advapi32.INSTANCE.ImpersonateLoggedOnUser(phUser.getValue())) {
                    win32ErrorRuntime("ImpersonateLoggedOnUser");
                }

                try {
                    // Kill the children processes
                    for (final Integer childPid : tree) {
                        try {
                            WindowsProcess.kill(childPid, 1);
                        } catch (Exception e) {
                            // If a problem occurs still try to kill the next one
                            e.printStackTrace();
                        }
                    }
                } finally {
                    // Revert to self
                    if (!Advapi32.INSTANCE.RevertToSelf()) {
                        win32ErrorRuntime("RevertToSelf");
                    }

                    // Close handle
                    WindowsProcess.closeSafely(phUser);
                }
            }
        } finally {
            // Always close the streams and the handles
            if (this.inputStream != null) {
                try {
                    this.inputStream.close();
                } catch (Exception e) {
                }
                this.inputStream = null;
            }
            WindowsProcess.closeSafely(this.inWrite);

            if (this.outputStream != null) {
                try {
                    this.outputStream.close();
                } catch (Exception e) {
                }
                this.outputStream = null;
            }
            WindowsProcess.closeSafely(this.outRead);

            if (this.errorStream != null) {
                try {
                    this.errorStream.close();
                } catch (Exception e) {
                }
                this.outputStream = null;
            }
            WindowsProcess.closeSafely(this.errRead);

            this.destroyed = true;
        }
    }

    public void close() {
        if (this.handle != null && !this.handle.equals(Pointer.NULL)) {
            Kernel32.INSTANCE.CloseHandle(this.handle);
            this.handle = null;
        }
    }

    @Override
    public void finalize() throws Throwable {
        this.close();
    }

    /** Sets the private field 'handle' of a field descriptor */
    private void writefd(final FileDescriptor fd, final HANDLE pointer) {
        try {
            final Field handleField = FileDescriptor.class.getDeclaredField("handle");
            handleField.setAccessible(true);
            handleField.setLong(fd, Pointer.nativeValue(pointer.getPointer()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return this.errorStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * Gets the process tree.
     * !! This method cannot work without impersonation !!
     * 
     * @param pid the pid
     * @return the process tree
     */
    private static List<Integer> getProcessTree(final int pid) {

        final List<Integer> pids = new ArrayList<Integer>();
        pids.add(new Integer(pid));

        final HANDLE processes = MyKernel32.INSTANCE.CreateToolhelp32Snapshot(MyKernel32.TH32CS_SNAPPROCESS, 0);

        // The process can be childless 
        if (processes == null) {
            return pids;
        }

        final Map<Integer, PROCESSENTRY32> processMap = new HashMap<Integer, PROCESSENTRY32>();
        final Map<Integer, List<Integer>> childrenMap = new HashMap<Integer, List<Integer>>();

        final PROCESSENTRY32 me = new PROCESSENTRY32();
        me.szExeFile = new char[WinDef.MAX_PATH];
        me.dwSize = me.size();

        if (MyKernel32.INSTANCE.Process32First(processes, me)) {
            do {
                if (me.th32ProcessID > 0)
                    processMap.put(new Integer(me.th32ProcessID), me);
                if (me.th32ParentProcessID > 0 && processMap.get(new Integer(me.th32ParentProcessID)) != null) {
                    List<Integer> value = childrenMap.get(me.th32ParentProcessID);
                    if (value == null) {
                        value = new LinkedList<Integer>();
                        childrenMap.put(me.th32ParentProcessID, value);
                    }
                    value.add(me.th32ProcessID);
                }
            } while (MyKernel32.INSTANCE.Process32Next(processes, me));
        } else {
            System.out.println("cannot access first process in list ");
        }

        Kernel32.INSTANCE.CloseHandle(processes);

        return getProcessTree(childrenMap, pids);
    }

    /**
     * Gets the process tree.
     * @param childrenMap the children map
     * @param pids the pids
     * @return the process tree
     */
    private static List<Integer> getProcessTree(Map<Integer, List<Integer>> childrenMap, List<Integer> pids) {
        List<Integer> result = new ArrayList<Integer>();
        if (pids == null || pids.isEmpty()) {
            return result;
        }
        for (Integer i : pids) {
            // System.out.println(i);
            result.addAll(getProcessTree(childrenMap, childrenMap.get(i)));
        }
        result.addAll(pids);
        return result;
    }

    /**
     * Kill a process from its PID.
     * 
     * @param pid the PID
     * @param code the code
     * @return true, if successful
     */
    public static boolean kill(final int pid, final int code) {
        if (pid <= 0) {
            return false;
        }
        final HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(MyKernel32.PROCESS_TERMINATE, false, pid);
        if (hProcess == null) {
            win32ErrorRuntime("OpenProcess");
        }
        boolean result = false;
        try {
            result = Kernel32.INSTANCE.TerminateProcess(hProcess, code);
            if (!result) {
                win32ErrorRuntime("TerminateProcess");
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(hProcess);
        }
        return result;
    }

    /**
     * returns an IOException exception  with a Java-style error report from native implementation.
     * 
     * @param functionName The name of the native function the call has failed.
     */
    private static IOException win32ErrorIOException(final String functionName) {
        final int err = Kernel32.INSTANCE.GetLastError();
        final String mess = Kernel32Util.formatMessageFromLastErrorCode(err);
        return new IOException(functionName + " error=" + err + ", " + mess);
    }

    /**
     * Java-style error report from native implementation.
     * Since a IOException exception cannot be thrown inside an undeclared
     * context (no try-catch or throws), it must be wrapped inside a RuntimeException.
     * 
     * @param functionName The name of the native function the call has failed.
     */
    private static RuntimeException win32ErrorRuntime(final String functionName) {
        return new RuntimeException(win32ErrorIOException(functionName));
    }

    /**
     * Safely closes a handle.
     * @param ref handle by referencef
     */
    private static void closeSafely(final HANDLEByReference ref) {
        final HANDLE handle = ref.getValue();
        if (handle != null) {
            Kernel32.INSTANCE.CloseHandle(handle);
        }
    }
}
