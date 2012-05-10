@echo off
echo.
echo --- Dataspaces Example ----------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call ..\init.bat


set GCMA="hello\helloApplication.xml"
set GCMD="helloDeploymentLocal.xml"

%JAVA_CMD% -Dgcmdfile=%GCMD% org.objectweb.proactive.examples.dataspaces.hello.HelloExample %GCMA%
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
