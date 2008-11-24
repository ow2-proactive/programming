@echo off
echo.
echo --- Hello World Web Service ---------------------------------------------
echo --- (this example needs Tomcat Web Server installed and running) --------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

%JAVA_CMD% -Dproactive.http.port=8080 org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------