#!/bin/sh


# always base everything relative to this file to make it simple
# NOTE this won't work if you hack your `cd` command, like I normally do, e.g., having it call `ll` after running `cd`

env_vars_parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
export INTERTEXTUALITY_GRAPH_PROJECT_DIR=$env_vars_parent_path/../..
echo $INTERTEXTUALITY_GRAPH_PROJECT_DIR
export INTERTEXTUALITY_GRAPH_SCRIPTS_DIR=$INTERTEXTUALITY_GRAPH_PROJECT_DIR/scripts
export INTERTEXTUALITY_GRAPH_ETL_TOOLS_DIR=$INTERTEXTUALITY_GRAPH_PROJECT_DIR/etl-tools/
export INTERTEXTUALITY_GRAPH_MODELS_DIR=$INTERTEXTUALITY_GRAPH_PROJECT_DIR/models/
export INTERTEXTUALITY_GRAPH_RAW_DATA_DIR=$INTERTEXTUALITY_GRAPH_ETL_TOOLS_DIR/raw-data-files/
export INTERTEXTUALITY_GRAPH_PLAY_API_DIR=$INTERTEXTUALITY_GRAPH_PROJECT_DIR/play-app
export INTERTEXTUALITY_GRAPH_MIGRATIONS_DIR=$INTERTEXTUALITY_GRAPH_ETL_TOOLS_DIR/db/migrations/
export INTERTEXTUALITY_GRAPH_DATA_UTILS_DIR=$INTERTEXTUALITY_GRAPH_PROJECT_DIR/data-utils-for-java/
export INTERTEXTUALITY_GRAPH_KEYSPACE="intertextuality_graph"

# TODO all other script files should use these same vars by using `. ../setup/env-vars.sh`
