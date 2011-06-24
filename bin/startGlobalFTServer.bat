@echo off
REM This script starts the default fault tolerance server. See org.objectweb.proactive.core.body.ft.servers.StartFTServer
REM Usage : startGlobalFTServer [-proto cic|pml] [-name name] [-port portnumber] [-fdperiod faultDetectionPeriod (sec)] 
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..

call ".\init.bat"

%JAVA_CMD% -Xms64m -Xmx1024m org.objectweb.proactive.core.body.ft.servers.StartFTServer %*

ENDLOCAL
pause
