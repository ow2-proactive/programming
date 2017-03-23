@echo off
rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..

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
IF EXIST "%PROACTIVE%\classes\Core" (
	SET CLASSPATH=%CLASSPATH%;%PROACTIVE%\classes\Core;%PROACTIVE%\classes\Extensions;%PROACTIVE%\classes\Extra;%PROACTIVE%\classes\Utils
	SET JARS=%PROACTIVE%\lib
	FOR %%j IN ("%PROACTIVE%\lib\*.jar") DO SET JARS=!JARS!;%%j
	SET CLASSPATH=%CLASSPATH%;%JARS%
) ELSE (
	SET CLASSPATH=%CLASSPATH%;%PROACTIVE%\dist\lib\ProActive.jar;%PROACTIVE%\dist\lib\ProActive_utils.jar
)


set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Dproactive.home="%PROACTIVE%"  -Djava.security.manager -Djava.security.policy="%PROACTIVE%\examples\proactive.java.policy"

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
