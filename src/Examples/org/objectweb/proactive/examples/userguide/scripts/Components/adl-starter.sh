#!/bin/sh

echo
echo --- User Guide: ADL Starter ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"
$JAVACMD org.objectweb.proactive.examples.userguide.components.adl.starter.Main "$@"

echo
echo ------------------------------------------------------------
