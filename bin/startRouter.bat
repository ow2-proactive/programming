@echo off
echo.
echo --- StartRouter----------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD%  org.objectweb.proactive.extensions.pamr.router.Main %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
