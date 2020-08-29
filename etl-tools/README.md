# NOTE out of date, and just copied from my podcast tool

## How to start
### Import data from Tyndale Data set
```
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

