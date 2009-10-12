#!/bin/sh

echo
echo --- Undeploy Active Object Web Service ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD -Dproactive.http.port=8082 org.objectweb.proactive.examples.webservices.helloWorld.Undeploy "$@"

echo
echo ------------------------------------------------------------
