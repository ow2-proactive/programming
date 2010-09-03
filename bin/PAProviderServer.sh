#!/usr/bin/env bash
# PA Provider command script
#

workingDir=`dirname $0`
workingDir=`cd "$dirname"; pwd`

. ${workingDir}/env.sh

if [ $# = 0 ]; then
	echo "Usage: PAProviderServer COMMAND [options]"
	echo "where COMMAND is one of the following:"
	echo "  start		start the PAProvider server"
	echo "For a command help try COMMAND --help"
	exit 1
fi

command=$1
shift

# what command to run?
if [ "$command" = "start" ]; then
	CLASS="org.objectweb.proactive.extensions.vfsprovider.console.PAProviderServerStarter"
fi

$JAVACMD "$CLASS" "$@"
