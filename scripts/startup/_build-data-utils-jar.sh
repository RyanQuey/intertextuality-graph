#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../config/env-vars.sh

# only need to package if sending to scala lib dir
mvn -f $INTERTEXTUALITY_GRAPH_DATA_UTILS_DIR/pom.xml clean package

# depends on project version number
# TODO just have play import from mvn repo also
cp $INTERTEXTUALITY_GRAPH_DATA_UTILS_DIR/target/data-utils-$DATA_UTILS_VERSION.jar $INTERTEXTUALITY_GRAPH_PLAY_API_DIR/lib

# install to mvn repo
$INTERTEXTUALITY_GRAPH_DATA_UTILS_DIR/scripts/install-data-utils-jar.sh
