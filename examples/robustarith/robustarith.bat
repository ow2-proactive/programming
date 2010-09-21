@echo off
echo.
echo --- Robust Arithmetic ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
call ..\init.bat

set XMLDESCRIPTOR=GCMA.xml
%JAVA_CMD% org.objectweb.proactive.examples.robustarith.Main %XMLDESCRIPTOR%
ENDLOCAL

:end
pause
echo.
echo -----------------------------------------------------------------
