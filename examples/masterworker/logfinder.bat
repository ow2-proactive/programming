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


:start
set XMLDESCRIPTOR=GCMA.xml

set /A found=0
for %%i in (%*) do (
    if "%%i" == "-d" then set /a found=1
)


:launch

if %found% EQU 1 (
  %JAVA_CMD% org.objectweb.proactive.examples.masterworker.LogFinderExample %*
) ELSE (
  %JAVA_CMD% org.objectweb.proactive.examples.masterworker.LogFinderExample -d "%XMLDESCRIPTOR%" %*
)
ENDLOCAL

pause
echo.
echo ----------------------------------------------------------
echo on

:end
