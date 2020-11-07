#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# puts this jar into local mvn repo, so can be imported into project through the pom

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../../scripts/config/env-vars.sh

# seems to get teh ds java driver generated classes to compile right more consistently
rm -rf $INTERTEXTUALITY_GRAPH_MODELS_DIR/target

version=$INTERTEXTUALITY_GRAPH_MODELS_VERSION
artifactId=models

cd $INTERTEXTUALITY_GRAPH_MODELS_DIR && \
  # we don't need all this extra stuff, make it simple
  mvn clean install


# for play api
mvn package && \
cp $parent_path/../target/models-$version.jar $INTERTEXTUALITY_GRAPH_PLAY_API_DIR/lib

# mvn install:install-file \
#   -Dfile=$parent_path/../target/$artifactId-$version.jar \
#   -DgroupId=com.ryanquey.intertextuality-graph \
#   -DartifactId=models \
#   -Dversion=$version \
#   -Dpackaging=jar \
#   -DgeneratePom=true
