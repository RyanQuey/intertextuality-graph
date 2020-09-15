#!/bin/bash -eux
if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# runs sbt but loads env vars first. Can take up to three args and passes them in

# just packages
# does not get the required jars into the lib dir - for that, make sure to run intertextuality-graph/scripts/startup/_build-data-utils-jar.sh first

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
arg1=${1:-""}

# load all project environment variables
. $parent_path/../../../scripts/config/env-vars.sh && \

cd $INTERTEXTUALITY_GRAPH_PLAY_API_DIR

printf "\n\n== Using sbt ==\n" && \
if [ "$arg1 " != " " ]; then
  sbt ${arg1:-""} ${2:-""} ${3:-""}
else 
  # this one will run a console. Passing in even empty strings will not
  echo "Opening Console for play app"
  sbt
fi
