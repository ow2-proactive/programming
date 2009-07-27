#!/bin/sh

echo
echo --- Migration example for documentation ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

XMLDESCRIPTOR="$PROACTIVE_HOME/src/Examples/org/objectweb/proactive/examples/documentation/GCMDeployment/ApplicationDescriptor.xml"
$JAVACMD org.objectweb.proactive.examples.documentation.GCMDeployment.Main "$XMLDESCRIPTOR" "$@"

echo
echo ------------------------------------------------------------
