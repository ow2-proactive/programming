#!/bin/sh

echo
echo --- Fractal Helloworld example ---------------------------------------------
echo ---
echo --- The expected result is an exception
echo ---

workingDir=`dirname $0`
. ${workingDir}/../env.sh

#if [ $# -lt 1 ]; then
#    echo "
#       usage :
#         helloworld-fractal.sh <parameters>

#		optional parameters are :
#			- parser
#			- wrapper
#			- distributed (needs parser)
#    "
#    exit 1
#fi

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"

$JAVACMD org.objectweb.proactive.examples.components.helloworld.HelloWorld "$@"



echo
echo ---------------------------------------------------------
