#!/bin/sh

echo
echo --- Hello World Web Component  Service Call ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.examples.webservices.helloWorld.WSClientComponent "$@"

echo
echo ------------------------------------------------------------
 