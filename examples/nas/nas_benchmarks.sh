#!/bin/sh

if [ -z "$PROACTIVE" ]
then
	workingDir=`dirname $0`
	PROACTIVE=$workingDir/../../.
	CLASSPATH=.
fi

. ${workingDir}/../env.sh

default_class=S
default_np=1
default_descriptor=${workingDir}/GCMA.xml

if [[ $# -eq  0 ]]
then
	echo "Usage: $0 KERNEL [CLASS] [NP] [DESCRIPTOR]"
	echo "  KERNEL        Select the kernel to launch: EP, MG, CG, FT or IS"
	echo "  CLASS         Class of data size (from smallest to biggest): S, W, A, B, C, D. Default is $default_class"
	echo "  NP            Number of processes. Default is $default_np"
	echo "  DESCRIPTOR    GCM Application descriptor file. Default is $default_descriptor"
	exit 1
fi

echo
echo --- NAS Benchmarks with ProActive -----------------------



args="-kernel $1 -np ${3-$default_np} -class ${2-$default_class} -descriptor ${4-$default_descriptor}"

$JAVACMD org.objectweb.proactive.benchmarks.NAS.Benchmark $args

echo
echo ---------------------------------------------------------
