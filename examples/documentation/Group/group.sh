#!/bin/sh

echo
echo --- Group example for documentation ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

$JAVACMD $HTTP_OPT org.objectweb.proactive.examples.documentation.group.GroupA "$@"

echo
echo ------------------------------------------------------------
