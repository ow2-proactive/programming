@echo off
echo.
echo --- Hello World example ---------------------------------------------

goto doit

:usage
echo.
goto end

:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call ..\init.bat

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

REM JUST the hello launcher. No parameter. batch file asks a question.
if errorlevel 1 GOTO remote


:start
set XMLDESCRIPTOR=GCMA.xml

set /A found=0
for %%i in (%*) do (
    if "%%i" == "-d" then set /a found=1
)


:launch

if %found% EQU 1 (
  %JAVA_CMD% org.objectweb.proactive.examples.masterworker.BasicPrimeExample %*
) ELSE (
  %JAVA_CMD% org.objectweb.proactive.examples.masterworker.BasicPrimeExample -d "%XMLDESCRIPTOR%" %*
)
ENDLOCAL

pause
echo.
echo ----------------------------------------------------------
echo on

:end
