#!/bin/sh
echo
echo --- StartNode -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Start a ProActive node on a new runtime (new JVM). The URL
       of the node is printed on the standard output.

         startNode.sh <the node name of the node to create>

         ex : startNode.sh  node1

    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.core.node.StartNode "$@"

echo
echo ---------------------------------------------------------
