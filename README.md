# Intertextuality graph
Intertextuality Graph, a project using DSE Graph db, served over a Play Framework API, to chart biblical and extrabiblical intertextuality. Use case is as an aid in researching and presenting intertextual connections. 
![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/2-hops.mal-1-1.png)

## Setup Dev Env
If you need to use a db password or change from localhost DSE instance:
```
cp scripts/config/.env.sample  scripts/config/.env
```

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
- [Tyndale House's STEPBible Data](https://github.com/tyndale/STEPBible-Data). See their site for their license. Using especially `TOTHT - Tyndale OT Hebrew Tagged text` and `TANTT - Tyndale Amalgamated NT Tagged texts`, but might soon (or have already??) branched out to others as well.

- [Tyndale House, Cambridge](https://www.TyndaleHouse.com)
- ["STEP Bible"](https://www.STEPBible.org)
    * I also used the STEP bible in an iframe
- [Data source](https://tyndale.github.io/STEPBible-Data/)
- [Theographic Bible](https://github.com/robertrouse/theographic-bible-metadata)

### For Cross references:
- http://www.openbible.info/labs/cross-references/
- https://github.com/narthur/tsk-cli/blob/master/tskxref.txt


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
