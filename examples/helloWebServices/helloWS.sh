#!/bin/sh

echo
echo --- Hello World Web Service ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dproactive.http.port=8080"

$JAVACMD  org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld "$@"

echo
echo ------------------------------------------------------------
