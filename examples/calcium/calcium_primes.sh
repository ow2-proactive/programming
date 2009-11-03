#!/bin/sh

echo
echo --- Calcium example  ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.extensions.calcium.examples.findprimes.FindPrimes "$@"

echo
echo ------------------------------------------------------------
