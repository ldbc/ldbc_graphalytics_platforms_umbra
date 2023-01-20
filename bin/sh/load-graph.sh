#!/bin/bash

set -e

if [ "$(uname)" == "Darwin" ]; then
  ROOTDIR=$(dirname $(greadlink -f ${BASH_SOURCE[0]}))/../..
else
  ROOTDIR=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/../..
fi

# Parse commandline instructions (provided by Graphalytics).
while [[ $# -gt 1 ]] # Parse two arguments: [--key value] or [-k value]
  do
  key="$1"
  value="$2"

  case ${key} in

    --graph-name)
      GRAPH_NAME="$value"
      shift;;

    --input-directory)
      INPUT_DIRECTORY="$value"
      shift;;

    --input-vertex-path)
      INPUT_VERTEX_PATH="$value"
      shift;;

    --input-edge-path)
      INPUT_EDGE_PATH="$value"
      shift;;

    --output-path)
      OUTPUT_PATH="$value"
      shift;;

    --directed)
      DIRECTED="$value"
      shift;;

    --weighted)
      WEIGHTED="$value"
      shift;;

    *)
      echo "Error: invalid option: " "$key"
      exit 1
      ;;
  esac
  shift
done

mkdir -p ${OUTPUT_PATH}

export POSTGRES_INPUT_DATA_DIR=${INPUT_DIRECTORY}
${ROOTDIR}/bin/scripts/start-postgres.sh

${ROOTDIR}/bin/exe/load.py \
   --graph-name $GRAPH_NAME \
   --directed $DIRECTED \
   --weighted $WEIGHTED
