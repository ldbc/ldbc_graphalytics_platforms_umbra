#!/bin/bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )"
cd ..

git add -A
git commit -a --amend --no-edit
scripts/init.sh
cd graphalytics-1.5.0-SNAPSHOT-umbra-0.0.1-SNAPSHOT
bin/sh/run-benchmark.sh
