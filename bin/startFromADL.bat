@echo off
REM This script may be used to launch a Fractal/GCM component application 
REM describe with an ADL.
REM Usage:  
REM startFromADL.sh descriptor_file fractal_ADL_file
REM   descriptor_file   a deployment descriptor
REM   fractal_ADL_file  a fractal ADL file describing your components assembly 
REM                     (for an ADL file located in org/o/o/p/MyApp.fractal give
REM                     org.o.o.p.MyApp as parameter)

:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"
set JAVA_CMD=%JAVA_CMD% -Dfractal.provider=org.objectweb.proactive.core.component.Fractive
%JAVA_CMD% org.objectweb.proactive.examples.components.StartFromADL %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
