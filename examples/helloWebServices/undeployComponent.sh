#!/bin/sh

echo
echo --- Undeploy Component Web Service ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.examples.webservices.helloWorld.UndeployComponent "$@"

echo
echo ------------------------------------------------------------
