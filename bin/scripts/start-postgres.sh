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

# fix permission issues on Linux, see also
# https://stackoverflow.com/questions/34031397/running-docker-on-ubuntu-mounted-host-volume-is-not-writable-from-container
docker exec ${POSTGRES_CONTAINER_NAME} bash -c "chmod 777 /output-data"

echo -n "Waiting for the database to start ."
until python3 scripts/test-db-connection.py 1>/dev/null 2>&1; do
    docker ps | grep ${POSTGRES_CONTAINER_NAME} 1>/dev/null 2>&1 || (
        echo
        echo "Container lost."
        exit 1
    )
    echo -n " ."
    sleep 1
done
echo
echo "Database started"

scripts/create-db.sh
