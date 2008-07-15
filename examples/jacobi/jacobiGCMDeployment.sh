#!/bin/sh

workingDir=`dirname $0`
. ${workingDir}/../env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive -Djava.security.policy=../proactive.java.policy -Dlog4j.configuration=file:../proactive-log4j "

# ./jacobiGCMDeployment.sh GCMA.xml 100 multicast 2 2 100 100 -Dproactive.home=/user/cdalmass/home/workspace/ProActiveTrunk/

echo --- GCM application: Jacobi example ---------------------
$JAVACMD -Dproactive.net.interface=eth0 org.objectweb.proactive.examples.components.jacobi.JacobiGCMDeployment $*
echo ---------------------------------------------------------
