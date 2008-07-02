#!/bin/sh

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"


echo --- GCM application: tutorial ---------------------
$JAVACMD org.objectweb.proactive.examples.components.userguide.Main $*
echo ---------------------------------------------------------
