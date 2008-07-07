#!/bin/sh

echo
echo --- Agent Client ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

export XMLDESCRIPTOR=$workingDir/GCMA.xml
$JAVACMD org.objectweb.proactive.examples.migration.AgentClient $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
