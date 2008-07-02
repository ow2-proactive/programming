#!/bin/sh

echo
echo --- Agent Client ---------------------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. ${workingDir}/../../env.sh
export XMLDESCRIPTOR=$workingDir/GCMA.xml
$JAVACMD org.objectweb.proactive.examples.migration.AgentClient $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
