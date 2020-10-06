#!/bin/bash -eux

# script to run once when setting up a new development environment
if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

#########################################
# instructions: 
# start with bash NOT sh. Currently only works in bash
#########################################

# for more advanced try/catch stuff, see here https://stackoverflow.com/a/25180186/6952495
# not necessary for now though

# want to start this in a daemon, and asynchronously, since it takes a while.
# just make sure not to run the main jar file until Cassandra is ready
# TODO suppress logs in this console

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/config/env-vars.sh

# go ahead and make sure data-utils submodule is pulled

cd $parent_path
git submodule update --init --recursive

###########################
# Run migrations
##########################
$INTERTEXTUALITY_GRAPH_SCRIPTS_DIR/run_migrations.sh


#################################################
# START THE DOCKER CONTAINERS with docker-compose
#################################################

# fire everything up in one docker-compose statement
# Note that if it is in one docker-compose statement like this, it allows the separate services to talk to one another even though they have separate docker-compose yml files

################################################
# download and install janus-graph
################################################

# TODO add in when ready
# $INTERTEXTUALITY_GRAPH_SCRIPTS_DIR/startup/_setup-janus.sh



# TODO add conditional if jar exists already. Or even better, track changes in git and find out if need to rebuild jar...that's getting a little bit crazy though
$parent_path/startup/start-every-compose.sh

###########################
# Prepare the gatsby / react frontend
##########################
# NOTE TODO only necessary for development, will deploy to CDN for prod
$INTERTEXTUALITY_GRAPH_GATSBY_DIR/scripts/setup-dev.sh

echo "waiting for Cassandra to be available..." && \
CASSANDRA_IS_UP=false
while [[ $CASSANDRA_IS_UP == false ]]; do
  # keep running until last command in loop returns true

  docker exec i-graph-dse nodetool status | grep -q 'UN' && CASSANDRA_IS_UP=true
  if [[ $CASSANDRA_IS_UP == false ]]; then
    # TODO add a timeout or handle if cassandra is down
  	echo "Cassandra is not up yet, waiting and try again"
  	sleep 1s
  else 
    echo "Cassandra is up! Continuing"
    echo "***************************"
  fi

  # returns true if: nodetool status ran without erroring and there is substring 'UN' in the output.
  
  # if above returns false, will try again
done && \

# This assumes C* is up, which requires docker-compose to run
$parent_path/run_migrations.sh
