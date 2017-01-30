#!/bin/bash
# Script for wrapping MPICH MPI code with GCM Deployment --  <Guilherme.Peretti-Pezzi@sophia.inria.fr>
#
# How to use
#
# In this script:
#  -  point the mpiexec variables to your MPICH installation
#
# In the GCM Infrastructure XML descriptor file:
#  -  use this script instead of using directly mpiexec on "commandPath"
#
# For Example:
#        <infrastructure>
#
#		<mpiGroup id="LAN_INRIA_MPI" hostList="naruto cheypa tche"
#			commandPath="/path/to/mpich-wrapper.sh">
#		</mpiGroup>
#
#	</infrastucture>
#

mpi_path=$1
np=$2
machinefile="$3"
exec_dir="$4"

index=1
args=""
for arg in "$@"
do
  if [ $index -ge 5 ]
    then
      args="$args $arg"
  fi
  let "index+=1"
done

cd $exec_dir
echo "$mpi_path/bin/mpirun -np $np -machinefile $machinefile $args"

$mpi_path/bin/mpirun -np $np -machinefile $machinefile $args

exit 0
