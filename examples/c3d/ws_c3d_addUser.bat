@echo off
echo.
echo --- C3D Add User WS ---------------------------------------------
echo --- (this example needs Tomcat Web Server installed and running) --------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

set XMLDESCRIPTOR=GCMA_User.xml
%JAVA_CMD% -Dproactive.http.port=8080 org.objectweb.proactive.examples.webservices.c3dWS.WSUser %XMLDESCRIPTOR% %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
