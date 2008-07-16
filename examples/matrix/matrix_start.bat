@echo off
echo.
echo --- Matrix : nodes initialization ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

set XMLDESCRIPTOR=GCMA.xml
%JAVA_CMD% org.objectweb.proactive.examples.matrix.Main 300 %XMLDESCRIPTOR%
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
