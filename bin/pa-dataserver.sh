#!/usr/bin/env bash
# PA dataserver command script
#

workingDir=`dirname $0`
workingDir=`cd "$dirname"; pwd`

. ${workingDir}/env.sh

if [ $# = 0 ]; then
	echo "Usage: pa-dataserver.sh COMMAND [options]"
	echo "where COMMAND is one of the following:"
	echo "  start		start the ProActive data server"
	echo "For a command help try COMMAND --help"
	echo "ProActive system properties can be set using command line too."
    echo "  Syntax is: -Dproperty=value"
	exit 1
fi

command=$1
shift

# what command to run?
if [ "$command" = "start" ]; then
	CLASS="org.objectweb.proactive.extensions.vfsprovider.console.PADataserverStarter"
fi

$JAVACMD "$CLASS" "$@"
