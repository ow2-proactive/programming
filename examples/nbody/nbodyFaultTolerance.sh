#! /bin/sh

echo 'Starting Fault-Tolerant version of ProActive NBody...'

workingDir=`dirname $0`

. ${workingDir}/nbody.sh -displayft "$@"
