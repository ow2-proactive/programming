@echo off
echo.
echo --- NAS Example ---------------------------------------------

set DEFAULT_CLASS=S
set DEFAULT_NP=1
set DEFAULT_DESCRIPTOR=GCMA.xml

goto doit

:usage
echo.
echo Usage: $0 KERNEL [CLASS] [NP] [DESCRIPTOR]
echo 	KERNEL        Select the kernel to launch: EP, MG, CG, FT or IS
echo 	CLASS         Class of data size (from smallest to biggest): S, W, A, B, C, D. Default is %DEFAULT_CLASS%
echo 	NP            Number of processes. Default is %DEFAULT_NP%
echo 	DESCRIPTOR    GCM Application descriptor file. Default is %DEFAULT_DESCRIPTOR%
goto end

:doit
SETLOCAL ENABLEDELAYEDEXPANSION

if "%1"=="" goto usage

call ..\init.bat


if "%4"=="" (
	set DESCRIPTOR=%DEFAULT_DESCRIPTOR%
) else (
	set DESCRIPTOR=%4
)

if "%3"=="" (
	set NP=%DEFAULT_NP%
) else (
	set NP=%3
)

if "%2"=="" (
	set CLASS=%DEFAULT_CLASS%
) else (
	set CLASS=%2
)

set ARGS=-kernel %1 -np %NP% -class %CLASS% -descriptor %DESCRIPTOR%

set ARGS
%JAVA_CMD% org.objectweb.proactive.benchmarks.NAS.Benchmark %ARGS%

ENDLOCAL

pause
echo.
echo ----------------------------------------------------------
echo on

:end
