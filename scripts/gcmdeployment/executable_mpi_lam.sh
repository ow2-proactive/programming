#!/bin/bash
# Script for wrapping LAM MPI code with GCM Deployment --  <Guilherme.Peretti-Pezzi@sophia.inria.fr>
#
# How to use
#
# In this script:
#  -  point the lamboot, mpirun and lamhalt variables to your LAM installation 
#
# In the GCM Infrastructure XML descriptor file:
#  -  use this script instead of using directly mpirun on "commandPath"
#
# For Example:
#        <infrastructure>
#
#		<mpiGroup id="LAN_INRIA_MPI" hostList="naruto cheypa tche"
#			commandPath="/path/to/lam-wrapper.sh">
#		</mpiGroup>
#
#	</infrastucture>
#


mpi_path=$1
np=$2
machinefile="$3"

index=1
args=""
for arg in "$@"
do
  if [ $index -ge 4 ]
    then
      args="$args $arg"
  fi
  let "index+=1"
done         

lamboot=$mpi_path/bin/lamboot
lamrun=$mpi_path/bin/mpirun
lamhalt=$mpi_path/bin/lamhalt

# Local machine must be part of the lam universe (i.e. included in the machinefile)
echo "$lamboot -v $machinefile"
$lamboot -v $machinefile

echo "$mpirun $args"
$lamrun -v -np $np $args

echo "$lamhalt"
$lamhalt

exit 0
