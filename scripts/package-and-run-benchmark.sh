#!/bin/bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )"
cd ..

scripts/init.sh
cd graphalytics-1.8.0-umbra-0.0.1-SNAPSHOT

export POSTGRES_INPUT_DATA_DIR=${POSTGRES_INPUT_DATA_DIR:-~/graphs}
bin/scripts/start-postgres.sh
bin/sh/run-benchmark.sh
