#!/bin/sh

echo
echo --- Exception example for documentation ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

$JAVACMD org.objectweb.proactive.examples.documentation.exceptions.ExceptionHandling "$@"

echo
echo ------------------------------------------------------------
