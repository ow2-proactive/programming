@echo off
echo.
echo --- OSGi example using oscar  -------------------
call ..\init.bat
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..


del /F /-P cache\%COMPUTERNAME%


%JAVA_CMD% -Doscar.cache.profiledir="cache/%COMPUTERNAME%"  -Doscar.cache.profile="%COMPUTERNAME%"  -Djava.security.policy="etc\java.policy"  -Dcom.sun.management.jmxremote -Dproactive.http.servlet=true -Dproactive.communication.protocol=http -Dproactive.http.port=8080  -Doscar.system.properties="etc\system.properties"   -jar "%PROACTIVE%\dist\lib\oscar.jar"

pause
echo.
echo -------------------------------------------------\

