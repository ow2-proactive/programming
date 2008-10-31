@echo off
echo.
echo --- Migrate ---------------------------------------------

IF [%2]==[] GOTO usage

goto doit

:usage
echo  Migrate an ActiveObject from Node1 to Node2
echo  migrate.bat the_url_of_the_source_node the_url_of_the_destination_node
echo      ex : migrate.bat  rmi://%HOSTNAME%/Node1 rmi://%HOSTNAME%/Node2
echo      ex : migrate.bat jini://%HOSTNAME%/Node1 rmi://%HOSTNAME%/Node2
echo.
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"

%JAVA_CMD% org.objectweb.proactive.examples.migration.SimpleObjectMigration %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
