@echo off
echo.
echo --- StartRouter----------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD%  org.objectweb.proactive.extensions.ssl.KeyStoreCreator %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
