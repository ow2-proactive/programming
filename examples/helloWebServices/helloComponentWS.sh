#!/bin/sh

echo
echo --- Hello World Component Web Service ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JVMARGS="-Dproactive.http.port=8080"
JVMARGS=$JVMARGS" -Dgcm.provider=org.objectweb.proactive.core.component.Fractive"

$JAVACMD $JVMARGS org.objectweb.proactive.examples.webservices.helloWorld.HelloWorldComponent "$@"

echo
echo ------------------------------------------------------------
