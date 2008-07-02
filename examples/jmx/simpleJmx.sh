#!/bin/sh

echo
echo --- JMC Test client connector---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh



$JAVACMD org.objectweb.proactive.examples.jmx.TestClient

echo
echo ------------------------------------------------------------
