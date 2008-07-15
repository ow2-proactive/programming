#!/bin/sh

for  mach in $(cat ../../../descriptors/nodes.properties | sed 's/NODES=//' | tr ' ' '\n'|uniq ); 
do 
  echo $mach;
  ssh $mach killall java &
done
