@echo off
echo.
echo --- User Guide: Sequential Primality Test ------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION

call ..\init.bat

%JAVA_CMD% -Dos=windows org.objectweb.proactive.examples.userguide.primes.sequential.Main %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------