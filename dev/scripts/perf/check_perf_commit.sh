#!/bin/sh

# Usage:
#       check_perf_jdk commit1 commit2 commit3 ...
#
# This script runs all the performance tests on several commits and
# compare the results. PNG files are created in PWD. It only works on a
# git repository
#
# A temporary local branch is created to run the tests
#


TMPDIR=$(mktemp -d)

until [ -z "$1" ]
do
    commit="$1"
    shift

    echo "Creating branch for $commit"
    ORIG_BRANCH=$(git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1 /')
    git co -b __TMP_PERF_GRAPH $commit
    rm performanceTests.*
    ./compile/build clean junit.performance
    git co $ORIG_BRANCH
    git branch -D __TMP_PERF_GRAPH

    mkdir $TMPDIR/$commit
    cp *.Test* $TMPDIR/$commit
done
python $(dirname $0)/perf_graph.py $TMPDIR  performanceTests.bandwidth  performanceTests.throughput
