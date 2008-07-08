@echo off
echo.
echo --- Agent ----------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

set XMLDESCRIPTOR=GCMA.xml

%JAVA_CMD% org.objectweb.proactive.examples.migration.AgentClient %XMLDESCRIPTOR%
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
