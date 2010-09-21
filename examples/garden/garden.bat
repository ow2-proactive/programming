@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call ..\init.bat

%JAVA_CMD% org.objectweb.proactive.core.node.StartNode vm1 &
%JAVA_CMD% org.objectweb.proactive.core.node.StartNode vm2 &

%JAVACMD% org.objectweb.proactive.examples.garden.Flower


ENDLOCAL

echo.
echo ---------------------------------------------------------
