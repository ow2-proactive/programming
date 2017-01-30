#!/usr/bin/env bash
# Naming service command script
#

workingDir=`dirname $0`
workingDir=`cd "$dirname"; pwd`

. ${workingDir}/env.sh

if [ $# = 0 ]; then
    echo "Usage: namingService COMMAND [options]"
    echo "where COMMAND is one of the following:"
    echo "  start		start the NamingService"
    echo "  ls		list the NamingService content"
#	echo "  add		add input into NamingService"
    echo "For a command help try COMMAND --help"
    exit 1
fi

command=$1
shift

# what command to run?
if [ "$command" = "start" ]; then
    CLASS="org.objectweb.proactive.extensions.dataspaces.console.NamingServiceStarter"
elif [ "$command" = "add" ]; then
    CLASS="org.objectweb.proactive.extensions.dataspaces.console.NamingServiceOperation"
elif [ "$command" = "ls" ]; then
    CLASS="org.objectweb.proactive.extensions.dataspaces.console.NamingServiceListing"
fi

$JAVACMD "$CLASS" "$@"
