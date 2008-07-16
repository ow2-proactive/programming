@echo off

echo Starting Fault-Tolerant version of ProActive NBody...

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call nbody.bat -displayft 4 3000

ENDLOCAL
pause