@echo off
echo.
echo --- User Guide: Migration CMAgent ------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION

call ..\init.bat

set XMLDESCRIPTOR=%TUTORIALS%\scripts\GCMDeployment\GCMA.xml
%JAVA_CMD% -Dos=windows org.objectweb.proactive.examples.userguide.cmagent.migration.Main "%XMLDESCRIPTOR%"
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------