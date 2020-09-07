#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

#########################################
# instructions: 
# start with bash NOT sh. Currently only works in bash
# add arg "rebuild" to rebuild java files into new jar and make new image from that. Also conveniently auto restarts those containers no matter what
#########################################

# for more advanced try/catch stuff, see here https://stackoverflow.com/a/25180186/6952495
# not necessary for now though

# want to start this in a daemon, and asynchronously, since it takes a while.
# just make sure not to run the main jar file until Cassandra is ready
# TODO suppress logs in this console

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../config/env-vars.sh

#################################################
# START THE DOCKER CONTAINERS with docker-compose
#################################################

# fire everything up in one docker-compose statement
# Note that if it is in one docker-compose statement like this, it allows the separate services to talk to one another even though they have separate docker-compose yml files

# TODO add conditional if jar exists already. Or even better, track changes in git and find out if need to rebuild jar...that's getting a little bit crazy though
$INTERTEXTUALITY_GRAPH_SCRIPTS_DIR/startup/_package-project.sh
docker-compose up -d 
