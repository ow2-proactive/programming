@echo off
echo.
echo --- User Guide: ADL Starter ------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION

call ..\init.bat

set JAVA_CMD=%JAVA_CMD% -Dgcm.provider="org.objectweb.proactive.core.component.Fractive"

%JAVA_CMD% org.objectweb.proactive.examples.userguide.components.adl.starter.Main %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------