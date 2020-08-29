#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

# always base everything relative to this file to make it simple
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
play_api_path=$parent_path/../play-app
migrations_path=$play_api_path/db/migrations
keyspace="intertextuality_graph"

# NOTE could alternatively make all migrations as classes that inherit from Migration and do something like: (from comment of https://stackoverflow.com/a/32439530/6952495)
# And if you want all jars in current directory on classpath too (eg. running Main class from lib directory) you can use java -cp "./*" com.mycomp.myproj.AnotherClassWithMainMethod Note the double quotes around ./* preventing unix machines from expanding it. Also note that "./*.jar" will not work

java -cp $play_api_path/lib/data-utils-0.4.0.jar com.ryanquey.datautils.cassandraHelpers.MigrationRunner $keyspace $migrations_path
