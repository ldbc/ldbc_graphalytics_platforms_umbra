#!/bin/bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )"
cd ..

cat graphalytics-*-umbra-*/report/*-UMBRA-report-CUSTOM/log/benchmark-full.log
