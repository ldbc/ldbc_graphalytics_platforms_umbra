#/bin/sh

set -eu

GRAPHS_DIR=${1:-~/graphs}

rootdir="$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )/.."
cd ${rootdir}
. scripts/project-vars.sh

rm -rf ${PROJECT}
mvn package -DskipTests -Dmaven.buildNumber.doCheck=false
tar xf ${PROJECT}-bin.tar.gz
cd ${PROJECT}/

cp -r config-template config
# set directories
sed -i.bkp "s|^graphs.root-directory =$|graphs.root-directory = ${GRAPHS_DIR}|g" config/benchmark.properties
sed -i.bkp "s|^graphs.validation-directory =$|graphs.validation-directory = ${GRAPHS_DIR}|g" config/benchmark.properties

bin/sh/compile-benchmark.sh
