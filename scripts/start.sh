#!/bin/bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )"
cd ..

. scripts/vars.sh

echo ${POSTGRES_DATABASE}

docker run \
    --rm \
    --detach \
    --publish=5432:5432 \
    --name ${POSTGRES_CONTAINER_NAME} \
    --env POSTGRES_USER=${POSTGRES_USER} \
    --env POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
    --env POSTGRES_DATABASE=${POSTGRES_DATABASE} \
    --volume=${POSTGRES_DATA_DIR}:/data:z \
    --volume=${POSTGRES_HOME}:/var/lib/postgresql/data:z \
    --shm-size=${POSTGRES_SHARED_MEMORY} \
    postgres:${POSTGRES_VERSION}
