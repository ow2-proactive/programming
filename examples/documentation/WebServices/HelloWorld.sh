#!/bin/sh

echo
echo --- Web Services example for documentation ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

OPTIONS="-Dproactive.http.port=8080"

$JAVACMD $OPTIONS org.objectweb.proactive.examples.documentation.webservices.Main "$@"

echo
echo ------------------------------------------------------------
