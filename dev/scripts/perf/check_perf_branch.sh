#!/bin/sh

# Usage:
#       check_perf_jdk branch1 branch2 branch3 ...
#
# This script runs all the performance tests on several branch and
# compare the results. PNG files are created in PWD
#
#
# This script only works with a git repository.



TMPDIR=$(mktemp -d)
ORIG_BRANCH=$(git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1 /')

until [ -z "$1" ]
do
    branch="$1"
    shift

    echo "Switching to $branch"
    git co $branch
    rm performanceTests.*
    ./compile/build clean junit.performance || exit 1

        branchdir=$(echo $branch | tr -d '/')

    mkdir -p $TMPDIR/$branchdir
    cp *.Test* $TMPDIR/$branchdir
done
python $(dirname $0)/perf_graph.py $TMPDIR  performanceTests.bandwidth  performanceTests.throughput
git co $ORIG_BRANCH
