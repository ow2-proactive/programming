#!/bin/sh

echo
echo --- Userguide CMAgent ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../../env.sh

export XMLDESCRIPTOR=file://$PROACTIVE/examples/userguide/GCMDeployment/GCMA.xml
$JAVACMD org.objectweb.proactive.examples.userguide.primes.distributedmw.PrimeExampleMW $XMLDESCRIPTOR "$@"

echo
echo ------------------------------------------------------------
