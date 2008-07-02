#! /bin/bash

workingDir=`dirname $0`
. ${workingDir}/env.sh

$JAVACMD -Djava.protocol.handler.pkgs=org.objectweb.proactive.core.ssh -Xms64m -Xmx1024m org.objectweb.proactive.core.body.ft.servers.StartFTServer "$@"

# DEBUG :   -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n -Djava.compiler=NONE
