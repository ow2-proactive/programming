@echo off
echo.
echo --- JMX Show OS ------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"


%JAVA_CMD%  org.objectweb.proactive.examples.jmx.ShowOS
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
