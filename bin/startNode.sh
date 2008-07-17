#!/bin/sh
echo
echo --- StartNode -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Start a ProActive node on a new runtime (new JVM) using the
       protocol specified in the url if any
         startNode.sh <the url of the node to create>

         ex : startNode.sh  node1 (start a node 'node1' using the default protocol on $HOSTNAME)
         ex : startNode.sh  ibis://$HOSTNAME/node1 (start a node 'node1' using the ibis protocol on $HOSTNAME)

    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.core.node.StartNode "$@"

echo
echo ---------------------------------------------------------
