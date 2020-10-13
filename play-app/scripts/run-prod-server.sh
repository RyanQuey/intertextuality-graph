#!/bin/bash -eux
if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# heroku requires running on dynamic port based off of environment variable
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
arg1=${1:-""}

# load all project environment variables
. $parent_path/../../scripts/config/env-vars.sh && \

cd $INTERTEXTUALITY_GRAPH_PLAY_API_DIR

sbt "run $PORT"
