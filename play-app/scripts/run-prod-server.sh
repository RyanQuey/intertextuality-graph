#!/bin/bash -eux
if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# heroku requires running on dynamic port based off of environment variable

sbt "run $PORT"
