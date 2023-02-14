#/bin/sh

set -eu

GRAPHS_DIR=${1:-~/graphs}
GRAPHALYTICS_VERSION=1.5.0-SNAPSHOT
PROJECT_VERSION=0.0.1-SNAPSHOT
PROJECT=graphalytics-${GRAPHALYTICS_VERSION}-umbra-${PROJECT_VERSION}

rm -rf ${PROJECT}
mvn package -DskipTests
tar xf ${PROJECT}-bin.tar.gz
cd ${PROJECT}/

cp -r config-template config
# set directories
sed -i.bkp "s|^graphs.root-directory =$|graphs.root-directory = ${GRAPHS_DIR}|g" config/benchmark.properties
sed -i.bkp "s|^graphs.validation-directory =$|graphs.validation-directory = ${GRAPHS_DIR}|g" config/benchmark.properties

bin/sh/compile-benchmark.sh
