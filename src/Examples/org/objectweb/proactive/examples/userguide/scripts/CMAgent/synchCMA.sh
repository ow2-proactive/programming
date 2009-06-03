#!/bin/sh

echo
echo --- User Guide: Synchronized CMAgent ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

export XMLDESCRIPTOR=$workingDir/../GCMDeployment/GCMA.xml
$JAVACMD org.objectweb.proactive.examples.userguide.cmagent.synch.Main $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
