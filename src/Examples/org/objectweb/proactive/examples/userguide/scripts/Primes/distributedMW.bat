@echo off
echo.
echo --- User Guide: Distributed Master-Worker Primality Test --------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION

call ..\init.bat

SET XMLDESCRIPTOR=file:\%TUTORIALS%\scripts\GCMDeployment\GCMA.xml
%JAVA_CMD% -Dos=windows org.objectweb.proactive.examples.userguide.primes.distributedmw.PrimeExampleMW "%XMLDESCRIPTOR%" %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------