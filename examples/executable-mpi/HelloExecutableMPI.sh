#!/bin/sh

echo
echo --- GCM native execution of MPI application: HelloWorld example -----------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

echo "Warning: Check variable from GCMD_LAM.xml and GCMD_MPICH.xml point to a valid mpi distribution"


#########################
# Application Descriptor
#########################
export XMLDESCRIPTOR=$workingDir/GCMA_MPI.xml

#########################
# Infrastructure Descriptor
#########################
if [ "$1" = "lam" ]
then
	export GCMD_DESCRIPTOR=$workingDir/GCMD_LAM.xml
else
	export GCMD_DESCRIPTOR=$workingDir/GCMD_MPICH.xml
fi

#########################
# Sample program to run 
#########################
EXAMPLE_NAME=hello_mpi

#########################
# MPI PROGRAM COMPILATION
#########################
echo "--- Compiling hello World example"
mpicc ${EXAMPLE_NAME}.c -o ${EXAMPLE_NAME}

#########################
# MPI PROGRAM EXECUTION THROUGH GCM
#########################
echo "--- Starting hello World example"
$JAVACMD -Ddeployment.gcmd=${GCMD_DESCRIPTOR} -Dexecutable.mpiname=${EXAMPLE_NAME} org.objectweb.proactive.examples.mpi.HelloExecutableMPI $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
