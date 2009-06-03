#!/bin/sh

echo
echo --- User Guide: Sequential Primality Test ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.examples.userguide.primes.sequential.Main "$@"

echo
echo ------------------------------------------------------------
