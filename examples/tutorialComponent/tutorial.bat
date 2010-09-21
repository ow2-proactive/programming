@echo off
echo.
echo --- GCM application: tutorial ---------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

set JAVA_CMD=%JAVA_CMD% -Dgcm.provider=org.objectweb.proactive.core.component.Fractive

%JAVA_CMD% org.objectweb.proactive.examples.components.userguide.Main %*
ENDLOCAL

pause
echo.
echo ---------------------------------------------------------