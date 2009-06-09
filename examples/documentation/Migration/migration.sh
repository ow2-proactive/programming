#!/bin/sh

echo
echo --- Migration example for documentation ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

$JAVACMD org.objectweb.proactive.examples.documentation.migration.Migration "$@"

echo
echo ------------------------------------------------------------
