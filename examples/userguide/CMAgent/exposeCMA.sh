#!/bin/sh

echo
echo --- Userguide CMAgent ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

CXF_HOME=$PROACTIVE/src/Extensions/org/objectweb/proactive/extensions/webservices/cxf
CLASSPATH=$CLASSPATH:$CXF_HOME/lib/cxf-manifest.jar
JAVACMD=$JAVACMD" -Dproactive.http.port=8080"
JAVACMD=$JAVACMD" -Djava.rmi.server.RMIClassLoaderSpi=org.objectweb.proactive.core.classloading.protocols.ProActiveRMIClassLoader"

$JAVACMD org.objectweb.proactive.examples.userguide.cmagent.webservice.CMAgentService "$@"

echo
echo ------------------------------------------------------------
