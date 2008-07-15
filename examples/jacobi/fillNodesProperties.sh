#!/bin/sh

echo "NODES= $(oarstat -fj $1 | grep assigned_hostnames | sed 's/assigned_hostnames//' |tr '+=' ' ')" > ../../../descriptors/nodes.properties 
