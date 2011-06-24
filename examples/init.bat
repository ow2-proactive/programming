@echo off
rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

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

rem Test if classes exists and is not empty
rem IF EXIST "%PROACTIVE%\classes\Core" (
rem  	SET CLASSPATH=!CLASSPATH!;%PROACTIVE%\classes\Core;%PROACTIVE%\classes\Extensions;%PROACTIVE%\classes\Extra;%PROACTIVE%\classes\Examples
rem 	SET JARS=%PROACTIVE%\lib
rem 	FOR %%j IN ("%PROACTIVE%\lib\*.jar") DO SET JARS=!JARS!;%%j
rem 	SET CLASSPATH=!CLASSPATH!;!JARS!
rem 	rem IF EXIST "%PROACTIVE%\ProActive_examples.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive_examples.jar
rem ) ELSE (
	SET CLASSPATH=%CLASSPATH%;%PROACTIVE%\dist\lib\ProActive.jar;%PROACTIVE%\dist\lib\ProActive_examples.jar;%PROACTIVE%\dist\lib\ibis-1.4.jar;%PROACTIVE%\dist\lib\ibis-connect-1.0.jar;%PROACTIVE%\dist\lib\ibis-util-1.0.jar
rem )

set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Dproactive.home="%PROACTIVE%"  -Dos="windows" -Djava.security.manager -Djava.security.policy="%PROACTIVE%\examples\proactive.java.policy"

rem Adding java tools to the path
SET OK=1
FOR /F "delims=;" %%i IN ("%PATH%") DO (
IF /I "%%i" == "%JAVA_HOME%\bin" SET OK=0
)
IF /I %OK%==1 SET PATH=%JAVA_HOME%\bin;%PATH%

goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer.
echo Use
echo    set JAVA_HOME=<the directory where is the JDK>
goto end


:end
