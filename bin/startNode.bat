@echo off
echo.
echo --- StartNode----------------------------------------

if "%1" == "" goto usage

goto doit

:usage
echo.
echo Start a ProActive node on a new runtime (new JVM)
echo The URL of the node is printed on the standard output.
echo    - 1 : the node name of the node to create
echo.
echo ex : startNode  node1
echo.
echo Node started with a random name
echo.
goto doit


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD%  org.objectweb.proactive.core.node.StartNode %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
