# Intertextuality graph

## Setup Dev Env
For the first time this project is being ran on a box (in development)

```
./scripts/setup.sh
```

## Start everything
```
./scripts/startup/start-every-compose.sh
```

### ETL Tools

```
cd ./etl-tools/
./scripts/sbt/sbt.sh compile
```

# Dataset used
[Tyndale House's STEPBible Data](https://github.com/tyndale/STEPBible-Data). See their site for their license. Using especially `TOTHT - Tyndale OT Hebrew Tagged text` and `TANTT - Tyndale Amalgamated NT Tagged texts`, but might soon (or have already??) branched out to others as well.

[Tyndale House, Cambridge](www.TyndaleHouse.com)
["STEP Bible"](www.STEPBible.org)
[Data source](tyndale.github.io/STEPBible-Data/)

## Data format
https://github.com/tyndale/STEPBible-Data#data-format

## DB migrations
see `./etl-scripts/db/README.md`

# Debugging
## Using my data utils
### Pulling the code
This is a submodule, so to use initially, you'll have to do something like:
```
git submodule update --init --recursive
```

### Making changes and seeing them reflect in other projects
1) Make the changes in the data utils code
2) You will want to change the version in the data-utils-for-java dir, so that other services recognize that the code has changed.
  - See file `data-utils-for-java/README.md` under "Versioning"
3) Use the install script so that it uses mvn install and makes the jar accessible to other projects via local maven repo

```
./data-utils-for-java/scripts/install-data-utils-jar.sh
```

4) If the changes are being reflected:
- If all else fails, consider nuking the repo and trying again
    ```
    cd ../
    rm -rf intertextuality-graph
    git clone https://github.com/RyanQuey/intertextuality-graph.git
    cd intertextuality-graph/
    ./scripts/setup.sh
    ```

    TODO if this happens often enough, create a script for nuking.
