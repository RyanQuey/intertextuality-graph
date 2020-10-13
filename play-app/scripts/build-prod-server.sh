#!/bin/bash -eux
if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# note: Ran in Heroku, not locally
# heroku requires running on dynamic port based off of environment variable
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
arg1=${1:-""}

sbt package
