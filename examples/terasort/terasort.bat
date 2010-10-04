@echo off
echo.
echo --- Terasort example ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
call ..\init.bat

if "%1" == "" (
	set XMLDESCRIPTOR=GCMA.xml
) else (
	set XMLDESCRIPTOR="%1"
)
%JAVA_CMD% -Dos=windows org.objectweb.proactive.examples.terasort.TeraSort %XMLDESCRIPTOR%
ENDLOCAL

:end
pause
echo.
echo -----------------------------------------------------------------
