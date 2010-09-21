@echo off
echo.
echo --- Reader / Writer ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
call ..\init.bat
%JAVA_CMD%  org.objectweb.proactive.examples.readers.AppletReader
ENDLOCAL

:end
pause
echo.
echo -----------------------------------------------------------------
