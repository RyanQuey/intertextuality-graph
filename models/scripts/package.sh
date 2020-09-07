#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# puts this jar into local mvn repo, so can be imported into project through the pom

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
. $parent_path/../../scripts/config/env-vars.sh

# make sure that data utils is in our mvn repo
$parent_path/../../data-utils-for-java/scripts/install-data-utils-jar.sh

cd $parent_path
mvn clean package
$parent_path/scripts/install-to-mvn-repo.sh
