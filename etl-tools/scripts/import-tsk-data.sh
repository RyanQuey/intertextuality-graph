#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

# load all project environment variables
. $parent_path/../../scripts/config/env-vars.sh

if [ $1 != "--skip-package" ]; then
  printf "\n\n== Packaging using sbt ==\n" && \
  $parent_path/sbt/sbt.sh clean package
fi

cd $parent_path/..
sbt "runMain com.ryanquey.intertextualitygraph.dataimporter.ImportTreasuryOfScriptureKnowledge"

