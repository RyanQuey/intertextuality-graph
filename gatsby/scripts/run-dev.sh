#!/bin/bash -eux

# script to run once when setting up a new development environment
if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../../scripts/config/env-vars.sh

# import for cloud9 to work
PORT=8080
IP=localhost

cd $INTERTEXTUALITY_GRAPH_GATSBY_DIR && \
  nvm use || true && \
  gatsby develop -p $PORT -H $IP
