@echo off
REM --- ProActive TryWithCatch annotator ------------------------------------
REM Usage:
REM trywithcatch.bat [-fullname] [-nobackup] FILES
REM   With the -fullname option every added call
REM   is prefixed with the full package org.objectweb.proactive.api
REM

:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..
call ".\init.bat"
%JAVA_CMD% trywithcatch.TryWithCatch %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
