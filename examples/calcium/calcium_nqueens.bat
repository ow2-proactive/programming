@echo off
echo.
echo --- Calcium Example ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

SET DESCRIPTOR=nqueens_args\GCMEnvironmentApplication.xml
SET VIRTUALNODE=local

%JAVA_CMD% org.objectweb.proactive.extensions.calcium.examples.nqueens.NQueens %DESCRIPTOR% %VIRTUALNODE% %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
