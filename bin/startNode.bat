@echo off
echo.
echo --- StartNode----------------------------------------

if "%1" == "" goto usage

goto doit

:usage
echo.
echo Start a ProActive node on a new runtime (new JVM)
echo  using the protocol specified in the url if any
echo    - 1 : the url of the node to create
echo.
echo ex : startNode  node1 (start a node 'node1' using the default protocol)
echo ex : startNode  ibis://localhost/node1 (start a node 'node1' using the ibis protocol)
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
