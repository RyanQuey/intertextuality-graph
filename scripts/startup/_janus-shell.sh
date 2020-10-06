#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../config/env-vars.sh

################################################
# start janus shell
# NOTE assumes janus server is already started
################################################
# doesn't work, sends the command but then closes it
# echo ":remote connect tinkerpop.server conf/remote.yaml session" | ~/lib/janusgraph-0.5.2/bin/gremlin.sh
~/lib/janusgraph-0.5.2/bin/gremlin.sh 
