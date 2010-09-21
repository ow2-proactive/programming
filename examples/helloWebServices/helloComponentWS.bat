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

SET JVMARGS=-Dproactive.http.port=8080
SET JVMARGS=%JVMARGS% -Dgcm.provider=org.objectweb.proactive.core.component.Fractive
%JAVA_CMD% %JVMARGS% org.objectweb.proactive.examples.webservices.helloWorld.HelloWorldComponent %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
