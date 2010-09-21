@echo off
echo.
echo --- Undeploy Web Service ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

%JAVA_CMD% -Dproactive.http.port=8080 org.objectweb.proactive.examples.webservices.helloWorld.UndeployComponent %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
