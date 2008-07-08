#!/bin/sh

#
# The following variable should be automatically
# assigned during install, if not, edit it to reflect
# your Java installation.
#
workingDir=`dirname $0`



#
# You don't need to edit the following line
#

OSCAR_HOME="."

echo ${HOSTNAME}
MYHOSTNAME=`hostname --long`
echo $MYHOSTNAME

rm -rf cache/${MYHOSTNAME}/*

${JAVA_HOME}/bin/java -Doscar.cache.profiledir="${OSCAR_HOME}/cache/${MYHOSTNAME}"  -Doscar.cache.profile="${MYHOSTNAME}"  -Djava.security.policy="${OSCAR_HOME}/etc/java.policy"  -Dcom.sun.management.jmxremote -Dproactive.http.servlet=true -Dproactive.communication.protocol=http -Dproactive.http.port=8080  -Doscar.system.properties="${OSCAR_HOME}/etc/system.properties"   -jar "$OSCAR_HOME/../../dist/lib/oscar.jar"

