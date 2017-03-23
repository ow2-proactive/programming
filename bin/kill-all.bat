@echo off

echo.
echo --- Killing ProActive Process ----------------------------------------

if "%1" == "" goto usage
goto doit

:usage
echo.
echo Please give a regular expression in parameter
echo Process which match this will be kill
echo.

:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call .\init.bat
SET CLASSPATH=%CLASSPATH%;%CD%\..\dev\lib\winp-1.5.jar
SET CLASSPATH=%CLASSPATH%;%CD%\..\dist\lib\ProActive_utils.jar
%JAVA_CMD% windowscleaner.WindowsCleaner ".*ProActive.jar.*"
%JAVA_CMD% windowscleaner.WindowsCleaner ".*proactive.home.*"
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
