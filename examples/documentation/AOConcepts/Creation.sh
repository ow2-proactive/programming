#!/bin/sh

echo
echo --- AO Creation example for documentation ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

$JAVACMD org.objectweb.proactive.examples.documentation.activeobjectconcepts.Creation "$@"

echo
echo ------------------------------------------------------------
