# just packages
# does not get the required jars into the lib dir - for that, make sure to run intertextuality-graph/scripts/startup/_build-data-utils-jar.sh first

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

# load all project environment variables
. $parent_path/../../../scripts/config/env-vars.sh && \

  printf "\n\n== Packaging using sbt ==\n" && \
  sbt compile

