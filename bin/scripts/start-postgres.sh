#!/bin/bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )"
cd ..

. scripts/vars.sh

scripts/stop-postgres.sh

docker run \
    --rm \
    --detach \
    --publish=${POSTGRES_PORT}:5432 \
    --name ${POSTGRES_CONTAINER_NAME} \
    --env POSTGRES_USER=${POSTGRES_USER} \
    --env POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
    --env POSTGRES_DATABASE=${POSTGRES_DATABASE} \
    --volume=${POSTGRES_INPUT_DATA_DIR}:/input-data:z \
    --volume=${POSTGRES_OUTPUT_DATA_DIR}:/output-data:z \
    --volume=${POSTGRES_HOME}:/var/lib/postgresql/data:z \
    --shm-size=${POSTGRES_SHARED_MEMORY} \
    postgres:${POSTGRES_VERSION}

#scripts/wait-for-it.sh --host=${POSTGRES_HOST} --port=${POSTGRES_PORT}
# ^ doesn't work for some reason ...

sleep 15

scripts/create-db.sh
