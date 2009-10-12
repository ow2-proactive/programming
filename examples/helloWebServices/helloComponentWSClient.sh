#!/bin/sh

echo
echo --- Hello World Web Component  Service Call ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"

$JAVACMD org.objectweb.proactive.examples.webservices.helloWorld.HelloWorldComponentClient "$@"

echo
echo ------------------------------------------------------------

