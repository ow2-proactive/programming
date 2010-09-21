@echo off
echo.
echo --- User Guide: Distributed Primality Test ------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION

call ..\init.bat

SET XMLDESCRIPTOR=%TUTORIALS%\scripts\GCMDeployment\GCMA.xml
%JAVA_CMD% -Dos=windows org.objectweb.proactive.examples.userguide.primes.distributed.Main "%XMLDESCRIPTOR%" %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------