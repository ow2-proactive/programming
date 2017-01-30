#!/bin/sh

# Usage:
#	check_perf_jdk JDKpath1 JDKpath2 JDKpath3
#
# This script runs all the performance tests used several JDKs
# PNG files are created in PWD
#

TMPDIR=$(mktemp -d)

OLD_JAVA_HOME=$JAVA_HOME
until [ -z "$1" ]
do
    jdk="$1"
    shift

    echo "Switching to JDK $jdk"
    export JAVA_HOME=$jdk
    rm performanceTests.*
    ./compile/build clean junit.performance || exit 1

    mkdir $TMPDIR/$(basename $jdk)
    cp *.Test* $TMPDIR/$(basename $jdk)
done
python $(dirname $0)/perf_graph.py $TMPDIR  performanceTests.bandwidth  performanceTests.throughput
