@echo off
echo.
echo --- C3D ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call ..\init.bat
set XMLDESCRIPTOR=GCMA_User.xml
%JAVA_CMD% org.objectweb.proactive.examples.c3d.C3DUser %XMLDESCRIPTOR%
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
