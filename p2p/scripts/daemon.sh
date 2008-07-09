#!/bin/sh

workingDir=`dirname $0`

. ${workingDir}/../../bin/env.sh

exec nice -19 $JAVACMD org.objectweb.proactive.extra.p2p.daemon.Daemon "$@"
