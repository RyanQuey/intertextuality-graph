# NOTE out of date, and just copied from my podcast tool

## How to start
### Import data from Tyndale Data set
```
cd ./etl-tools
sbt package

# can't do sbt run after we add other main classes
sbt "runMain com.ryanquey.intertextualitygraph.dataimporter.ImportTyndaleStepData"
```

## Setup For Development
### Install Scala
- Using [sbt](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html) rather than IntelliJ
- We want Scala 2.11 since that is what current DSE 6.8 uses and what we have setup in Zeppelin, etc

```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt-get update
sudo apt-get install sbt

# Debugging
## object models is not a member of package com.ryanquey.intertextualitygraph
- Make sure models project is packaged and installed into mvn repo

```
models/scripts/install-to-mvn-repo.sh
```

- Make sure also that the version number set in the config/env-vars.sh is same as what is set in the models project's pom.xml file.
- If all else fails, nuke the repo and restart (see main git repo README for how to nuke)
