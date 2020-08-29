#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

printf "\n\n== Packaging using sbt ==\n" && \
. $parent_path/../../scripts/config/env-vars.sh

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd $parent_path/..
sbt package
sbt "runMain com.ryanquey.intertextualitygraph.dataimporter.ImportTyndaleStepData"

