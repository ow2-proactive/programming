#!/bin/sh

echo
echo --- User Guide: ADL Composite ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dgcm.provider=org.objectweb.proactive.core.component.Fractive"
$JAVACMD org.objectweb.proactive.examples.userguide.components.adl.composite.Main "$@"

echo
echo ------------------------------------------------------------
