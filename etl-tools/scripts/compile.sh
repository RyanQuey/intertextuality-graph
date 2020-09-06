# just packages
# does not get the required jars into the lib dir - for that, make sure to run intertextuality-graph/scripts/startup/_build-data-utils-jar.sh first


  printf "\n\n== Packaging using sbt ==\n" && \
  sbt package

