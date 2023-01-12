#!/bin/bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ..

. scripts/vars.sh

export PGPASSWORD=${POSTGRES_PASSWORD}

psql -h localhost -U postgres -p 5432 -d ${POSTGRES_DATABASE}
