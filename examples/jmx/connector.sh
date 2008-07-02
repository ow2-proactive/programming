#!/bin/sh

echo
echo ---JMX Test Connector---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh



$JAVACMD org.objectweb.proactive.examples.jmx.TestServer $1

echo
echo ------------------------------------------------------------
