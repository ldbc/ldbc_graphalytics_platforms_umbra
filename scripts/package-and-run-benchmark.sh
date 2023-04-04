#!/bin/bash

set -eu
set -o pipefail

rootdir="$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )/.."
cd ${rootdir}
. scripts/project-vars.sh

export POSTGRES_INPUT_DATA_DIR=${POSTGRES_INPUT_DATA_DIR:-~/graphs}

scripts/init.sh
cd graphalytics-${GRAPHALYTICS_VERSION}-umbra-${PROJECT_VERSION}

bin/scripts/start-postgres.sh
bin/sh/run-benchmark.sh
