#!/bin/bash -eux

# script to run once when setting up a new development environment
if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../../scripts/config/env-vars.sh

cd $INTERTEXTUALITY_GRAPH_GATSBY_DIR && \
  
  curl "http://localhost:9000/texts/all-values-along-path-for-ref?book=2+Peter&chapter=2&verse=1&hopsCount=1" > ./src/data/intertextuality-all-vertices-2Peter.json
