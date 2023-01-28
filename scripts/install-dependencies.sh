#!/usr/bin/env bash

set -eu
set -o pipefail

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ..

if [[ ! -z $(which yum) ]]; then
    sudo yum install -y postgresql-devel postgresql
elif [[ ! -z $(which apt-get) ]]; then
    sudo apt-get update
    sudo apt-get install -y libpq5 postgresql-client
fi

pip3 install --user --progress-bar off psycopg
