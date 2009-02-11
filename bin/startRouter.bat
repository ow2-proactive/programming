@echo off
echo.
echo --- StartRouter----------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD%  org.objectweb.proactive.extra.forwardingv2.router.Main %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
