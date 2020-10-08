#!/bin/bash -eux

if [ "$BASH" != "/bin/bash" ]; then
  echo "Please do ./$0"
  exit 1
fi

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

# load all project environment variables
. $parent_path/../../scripts/config/env-vars.sh
printf "\n\n== converting our csv to have parsed references ==\n" && \

cd $parent_path/js

tsk_file_path="${INTERTEXTUALITY_GRAPH_RAW_DATA_DIR}/treasury-of-scripture-knowledge/tsk-cli.txt"
tsk="tsk-true"

node convert-references.js $tsk_file_path $tsk


