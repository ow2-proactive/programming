#!/bin/sh

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"

# ./jacobi.sh ../../../descriptors/MatrixOldDep.xml 100 multicast 2 2 100 100 

#  -Djava.security.policy=../../../scripts/proactive.java.policy -Dlog4j.configuration=file:../../../scripts/proactive-log4j

echo --- GCM application: Jacobi example ---------------------
$JAVACMD -Dproactive.net.interface=eth0 org.objectweb.proactive.examples.components.jacobi.Jacobi $*
echo ---------------------------------------------------------
