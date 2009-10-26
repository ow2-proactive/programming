#!/bin/sh

echo
echo --- GCM native execution of ProActive/MPI application: HelloWorld example -----------------------

if [[  -n `which mpicc | grep no`  ||  `which mpicc | wc -l` -eq 0  ]]
then
    #no mpicc found
    echo "Error: this example requires an MPI installation"
    exit 1
fi


export PROACTIVE_HOME="$HOME/SHORT_MPI_Extension"

workingDir=`dirname $0`
. ${workingDir}/../../env.sh
export LD_LIBRARY_PATH="${PROACTIVE_HOME}/dist/lib/native/"


#########################
# MPI PROGRAM EXECUTION THROUGH GCM
#########################
echo "--- Starting hello World example" 

#workaround to guarantee same launching folder on mpifork, despite of strange NFS configs
#cd /tmp 

$JAVACMD -Dos=unix -Djava.library.path=$PROACTIVE/dist/lib/native -Ddiscogrid.runtime.type=AOB -Dproactive.home=$PROACTIVE_HOME -Djava.library.path=$LD_LIBRARY_PATH org.objectweb.proactive.extensions.nativecode.NativeStarter $PROACTIVE/examples/mpi/proactive_mpi/gcma.pa.xml  $PROACTIVE/examples/mpi/proactive_mpi/gcma.mpi1.xml $PROACTIVE/examples/mpi/proactive_mpi/gcma.mpi2.xml

#cd -



echo
echo ---------------------------------------------------------
