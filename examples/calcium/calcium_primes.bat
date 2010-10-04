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

%JAVA_CMD% org.objectweb.proactive.extensions.calcium.examples.findprimes.FindPrimes %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
