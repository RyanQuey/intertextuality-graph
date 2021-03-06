#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

#########################################
# instructions: 
#########################################

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

$parent_path/_setup-for-compose.sh && \
docker-compose up -d --build
