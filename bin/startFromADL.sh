#!/bin/sh
# This script may be used to launch a Fractal/GCM component application 
# describe with an ADL.
# Usage:  
# startFromADL.sh descriptor_file fractal_ADL_file
#   descriptor_file   a deployment descriptor
#   fractal_ADL_file  a fractal ADL file describing your components assembly 
#                     (for an ADL file located in org/o/o/p/MyApp.fractal give
#                     org.o.o.p.MyApp as parameter)

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi

. $PROACTIVE/scripts/unix/env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"

echo --- Fractal ADL Launcher --------------------------------
$JAVACMD org.objectweb.proactive.examples.components.StartFromADL "$@"
echo ---------------------------------------------------------
