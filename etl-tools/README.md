# NOTE out of date, and just copied from my podcast tool

## How to start
### Get books and verses and chapters in the db
Note might currently need to be called from the etl-tools dir
```
cd etl-tools/
./scripts/import-theographic-data.sh
```
### Add some texts and intertextual connections
```
./scripts/import-tsk-data.sh
```

### Import data from Tyndale Data set
NOTE currently not really doing much, but it's ready to once I figure out what I need from it


Old Way:
```
cd ./etl-tools
./scripts/sbt/sbt.sh package

# can't do sbt run after we add other main classes
sbt "runMain com.ryanquey.intertextualitygraph.dataimporter.ImportTyndaleStepData"
```

New way: Just use the script. it sets env vars and stuff for you too.

## Setup For Development
### Install Scala
- Using [sbt](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html) rather than IntelliJ
- We want Scala 2.11 since that is what current DSE 6.8 uses and what we have setup in Zeppelin, etc

```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt-get update
sudo apt-get install sbt

### Run autocompile
```
./scripts/sbt/sbt.sh ~compile
```

Then open another one with the sbt console going, and can call `run` every so often:
```
./scripts/sbt/sbt.sh 
# Then in the console: 
runMain com.ryanquey.intertextualitygraph.dataimporter.ImportTreasuryOfScriptureKnowledge

# or if you don't know which class you want:
run 

# and it will give some options
```
# Debugging
## object models is not a member of package com.ryanquey.intertextualitygraph
- Make sure models project is packaged and installed into mvn repo

```
models/scripts/install-to-mvn-repo.sh
```

- Make sure also that the version number set in the config/env-vars.sh is same as what is set in the models project's pom.xml file.
- If all else fails, nuke the repo and restart (see main git repo README for how to nuke)

# Getting the raw data
Check out the `./scripts` dir

- `format-tsk-csv.sh` will format existing tsk-cli.txt file to separate out the references for easy ingestion
- there's another one for downloading some of the data files...I forget where

# Parse references from data sources
We have a good start with a js script that will use npm libs to convert references into osis, which is easily machine readable.

`./scripts/format-tsk-csv.sh`

# Code Organization
TODO
