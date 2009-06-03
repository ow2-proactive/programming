@echo off
echo.
echo --- User Guide: Initialized CMAgent ------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION

call ..\init.bat

%JAVA_CMD% -Dos=windows org.objectweb.proactive.examples.userguide.cmagent.initialized.Main
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------