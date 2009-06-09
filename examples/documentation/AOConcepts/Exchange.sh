#!/bin/sh

echo
echo --- AO Call example for documentation ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

$JAVACMD org.objectweb.proactive.examples.documentation.activeobjectconcepts.Exchange "$@"

echo
echo ------------------------------------------------------------
