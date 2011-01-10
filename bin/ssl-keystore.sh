#!/usr/bin/env bash

workingDir=`dirname $0`
. $workingDir/env.sh

VM_ARGS="-Xmx512m -Xms512m -server"

$JAVACMD $VM_ARGS org.objectweb.proactive.extensions.ssl.KeyStoreCreator "$@"
