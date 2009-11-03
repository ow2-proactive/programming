@echo off
echo.
echo --- Calcium Example ---------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

call "..\init.bat"


SET DESCRIPTOR=blast_args\LocalDescriptor.xml
SET QUERY=blast_args\query.nt
SET DATABASE=blast_args\db.nt
SET FORMATBD=blast_args\formatdb
SET BLASTALL=blast_args\blastall

%JAVA_CMD% org.objectweb.proactive.extensions.calcium.examples.blast.Blast %DESCRIPTOR% %QUERY% %DATABASE% %FORMATBD% %BLASTALL%
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------
