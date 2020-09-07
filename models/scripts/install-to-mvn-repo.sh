#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# puts this jar into local mvn repo, so can be imported into project through the pom

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../../scripts/config/env-vars.sh

version=0.1.0
artifactId=models

cd $INTERTEXTUALITY_GRAPH_MODELS_DIR && \
mvn package && \
mvn install:install-file \
  -Dfile=$parent_path/../target/$artifactId-$version.jar \
  -DgroupId=com.ryanquey.intertextuality-graph \
  -DartifactId=models \
  -Dversion=$version \
  -Dpackaging=jar \
  -DgeneratePom=true
