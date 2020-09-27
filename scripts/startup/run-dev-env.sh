#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# NOTE will only work after setting up/packaging jars (use setup.sh)

#########################################
# instructions: 
#########################################

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

docker start i-graph-dse

# TODO start play and gatsby

