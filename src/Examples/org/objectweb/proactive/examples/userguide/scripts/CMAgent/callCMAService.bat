@echo off
echo.
echo --- Hello World Web Service ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

%JAVA_CMD% org.objectweb.proactive.examples.userguide.cmagent.webservice.CMAgentWebServiceClient %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
