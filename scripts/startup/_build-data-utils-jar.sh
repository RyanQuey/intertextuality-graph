#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
data_utilities_path=$parent_path/../../data-utils-for-java
play_api_path=$parent_path/../../play-app

mvn -f $data_utilities_path/pom.xml package

# depends on project version number
cp $data_utilities_path/target/data-utils-0.4.0.jar $play_api_path/lib
