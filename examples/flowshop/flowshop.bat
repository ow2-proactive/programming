@echo off
echo.
echo --- FlowShop ----------------------------------------

goto doit

:usage
echo.
echo flowshop.bat
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call ..\init.bat

%JAVA_CMD% org.objectweb.proactive.examples.flowshop.Main -bench taillard\test_10_10.txt -desc GCMA.xml

ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
