@echo off
rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED TUTORIALS set TUTORIALS=%CD%\..\..

rem ----------------------------------------------------------------------------


if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

rem ----
rem Set up the classpath using classes dir or jar files
rem

IF DEFINED CLASSPATHEXT (
	SET CLASSPATH=%CLASSPATHEXT%
) ELSE (
	SET CLASSPATH=.
)

SET CLASSPATH=%CLASSPATH%;%TUTORIALS%\dist\lib\ProActive.jar
SET CLASSPATH=%CLASSPATH%;%TUTORIALS%\classes

set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Dproactive.home="%TUTORIALS%"  -Dos="windows" -Djava.security.manager -Djava.security.policy="%TUTORIALS%\scripts\proactive.java.policy"

goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer.
echo Use
echo    set JAVA_HOME=<the directory where is the JDK>
goto end


:end
