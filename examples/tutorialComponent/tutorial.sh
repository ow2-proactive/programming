#!/bin/sh

echo
echo --- GCM application: tutorial ---------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dgcm.provider=org.objectweb.proactive.core.component.Fractive"

$JAVACMD org.objectweb.proactive.examples.components.userguide.Main $*

echo
echo ---------------------------------------------------------
