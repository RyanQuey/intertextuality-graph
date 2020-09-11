#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

# load all project environment variables
. $parent_path/../../scripts/config/env-vars.sh
printf "\n\n== Packaging using sbt ==\n" && \

$parent_path/sbt/sbt.sh clean package
cd $parent_path/..
sbt "runMain com.ryanquey.intertextualitygraph.dataimporter.ImportTheographicData"

