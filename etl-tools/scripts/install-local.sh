#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

# load all project environment variables
. $parent_path/../../scripts/config/env-vars.sh
printf "\n\n== Packaging using sbt ==\n" && \

# now doing it this way, for the sake of heroku
# $parent_path/sbt/sbt.sh publishLocal
$parent_path/sbt/sbt.sh package
cp $parent_path/../target/models-$INTERTEXTUALITY_GRAPH_MODELS_VERSION.jar $INTERTEXTUALITY_GRAPH_PLAY_API_DIR/lib
