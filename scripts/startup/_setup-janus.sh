#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../config/env-vars.sh

################################################
# download and install janus-graph
################################################
curl -LO https://github.com/JanusGraph/janusgraph/releases/download/v0.5.2/janusgraph-0.5.2.zip
# this is where I like my binaries to live
mkdir -p ~/lib
unzip janusgraph-0.5.2.zip -d ~/lib/

# cleanup local archive
rm janusgraph-0.5.2.zip

# Now, setup for cql and es
cd ~/lib/janusgraph-0.5.2
# ...
