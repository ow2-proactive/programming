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

set JVMARGS=-Dgcm.provider=org.objectweb.proactive.core.component.Fractive

%JAVA_CMD% %JVMARGS% org.objectweb.proactive.examples.webservices.helloWorld.HelloWorldComponentClient %*

ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------

