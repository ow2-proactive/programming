@echo off
echo.
echo --- OSGi example using felix  -------------------
call ..\init.bat
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..


del /F /-P cache\%COMPUTERNAME%

set FELIX_HOME=%CD%

%JAVA_CMD% -Dorg.osgi.framework.storage="%FELIX_HOME%\cache\%COMPUTERNAME%" -Dfelix.config.properties="file:%FELIX_HOME%\conf\config.properties" -Djava.security.manager -Djava.security.policy="conf\java.policy" -Dproactive.http.port=8081 -Dproactive.communication.protocol=http -Dproactive.http.jetty.xml="jetty.xml" -jar "%PROACTIVE%\dist\lib\felix.jar"

pause
echo.
echo -------------------------------------------------\

