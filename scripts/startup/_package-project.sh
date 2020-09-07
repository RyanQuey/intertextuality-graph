#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../config/env-vars.sh && \

# package data-utils (also installs to mvn repo, and puts jar in play app lib)
$parent_path/_build-data-utils-jar.sh && \

# package models
$INTERTEXTUALITY_GRAPH_MODELS_DIR/scripts/install-to-mvn-repo.sh && \

# package data importer tools (sbt)
$INTERTEXTUALITY_GRAPH_ETL_TOOLS_DIR/scripts/compile.sh
