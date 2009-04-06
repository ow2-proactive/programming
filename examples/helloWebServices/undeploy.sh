#!/bin/sh

echo
echo --- Undeploy Active Object Web Service ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.examples.webservices.helloWorld.Undeploy "$@"

echo
echo ------------------------------------------------------------
