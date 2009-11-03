#!/bin/sh

echo
echo --- Calcium example  ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

DESCRIPTOR=nqueens_args/GCMEnvironmentApplication.xml
VIRTUALNODE=local

$JAVACMD org.objectweb.proactive.extensions.calcium.examples.nqueens.NQueens $DESCRIPTOR $VIRTUALNODE "$@"

echo
echo ------------------------------------------------------------
