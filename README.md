# Intertextuality graph
Intertextuality Graph, a project using DSE Graph db, served over a Play Framework API, to chart biblical and extrabiblical intertextuality. Use case is as an aid in researching and presenting intertextual connections. 
![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/dse-studio.results.alludes_to.first-1000-results.png)


## Setup Dev Env
If you need to use a db password or change from localhost DSE instance:
```
cp scripts/config/.env.sample  scripts/config/.env
```

For the first time this project is being ran on a box (in development)
  - Note that this builds jars, installs jars to local mvn, and runs db migrations for you. 
  - This can be done manually if script stops halfway as well, just checkout the bash script and do it manually

```
./scripts/setup.sh
```

## Start everything
```
./scripts/startup/start-every-compose.sh
```

Then start gatsby and Play app 
TODO add notes here...for now just go to those directories and follow their instructions

## Load seed data
```
# load books, chs, and verses (vertices)
# NOTE should be idempotent, so just run again if it messes up in the middle.
./etl-tools/scripts/import-theographic-data.sh

# wait until complete. The next script will need the books, chapters, and verses all loaded in before running.
# HOWEVER script 

# Then, when import-theographic-data.sh is finished, load some edges
./etl-tools/scripts/import-tsk-data.sh
```

## DSE Studio
DSE Studio is set up to start in a docker container. It will be started when you run the `start-every-compose.sh` script. You can view it at http://localhost:9091.

### Configure Connection to DSE 
- You will have to change the connection to use host: `dse` instead of `127.0.0.1`, since dse is running over docker, and `dse` is the name given in the the docker-compose.yml file. No credential changes are required by default. 
![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/dse-studio.instructions.configure-connection.png)

### Add Starter Notebook
Add some starter notebooks by uploading the tarballs from `intertextuality-graph/notebooks/dse-studio`.
![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/dse-studio.instructions.import-notebook.png)

Now find the notebook in the notebook index, and you can start playing around with the graph using Gremlin and CQL.


![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/dse-studio.results.alludes_to_Ps_40.diagram.color-by-book.png)

![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/dse-studio.results.alludes_to_Ps_50.diagram.color-by-book.zoomed-in.png)

## View Arc Diagram in Gatsby
### If everything started properly, you should be able to navigate to localhost:8080 and see the arc diagram
![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/diagram.2-hops.mal-1-1.png)


### You can go out multiple hops, and add filters for each hop. 
![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/diagram.2-hops.heb_to-Ps_to-Gen_1-2.png)


### The diagram is also itself interactive. Clicking on an edge or vertex shows properties for that graph element.
![screenshot](https://github.com/RyanQuey/intertextuality-graph/raw/master/screenshots/diagram.1-hop.rev_alludes-to-isa_40-56.with-selected-connection.png)



# Development

TODO fill this out more

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

# TODOs
- Consider moving fields to edges in order to improve read query performances for certain access patterns
- add `sys.exit(0)` to close processes

# Acknowledgements
For related works that help to provide ideas and inspiration for this project, see also:
- https://viz.bible/remaking-influential-cross-reference-visualization/
