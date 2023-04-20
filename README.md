# Postgres/Umbra Graphalytics implementation

[![Build Status](https://circleci.com/gh/ldbc/ldbc_graphalytics_platforms_umbra.svg?style=svg)](https://app.circleci.com/pipelines/github/ldbc/ldbc_graphalytics_platforms_umbra)

Implementation of LDBC Graphalytics using [PostgreSQL](https://www.postgresql.org/) and [Umbra](https://umbra-db.com/).

Pointers:

* [Graphalytics specification](https://ldbcouncil.org/ldbc_graphalytics_docs/graphalytics_spec.pdf)
* [Graphalytics website](https://ldbcouncil.org/benchmarks/graphalytics/)

### Building the project and running the benchmark

1. To initialize the benchmark package, run:

    ```bash
    scripts/init.sh ${GRAPHS_DIR}
    ```

    where `GRAPHS_DIR` is the directory of the graphs and the validation data. The argument is optional and its default value is `~/graphs`.

    This script creates a Maven package (`graphalytics-${GRAPHALYTICS_VERSION}-umbra-${PROJECT_VERSION}.tar.gz`). Then, it decompresses the package, initializes a configuration directory `config` (based on the content of the `config-template` directory) and sets the location of the graph directory.

    Note that the project uses the [Build Number Maven plug-in](https://www.mojohaus.org/buildnumber-maven-plugin/) to ensure reproducibility. Hence, builds fail if the local Git repository contains uncommitted changes. To build it regardless (for testing), run it as follows:

    ```bash
    scripts/init-for-testing.sh ${GRAPHS_DIR}
    ```

1. Navigate to the directory created by the `init.sh` script:

    ```bash
    cd graphalytics-*-umbra-*/
    ```

1. Edit the configuration files (e.g. graphs to be included in the benchmark) in the `config` directory. To conduct benchmark runs, edit the `config/benchmark.properties` file and replace the `include = benchmarks/custom.properties` to select the dataset size you wish to use, e.g. `include = benchmarks/xl.properties`

1. To set up a Postgres instance, run e.g.:

    ```bash
    export POSTGRES_INPUT_DATA_DIR=~/graphs
    bin/scripts/start-postgres.sh
    ```

1. Run the benchmark with the following command:

    ```bash
    bin/sh/run-benchmark.sh
    ```

## Testing the package

If you would like to initialize the benchmark and run it with the default configuration, run the following:

```bash
scripts/package-and-run-benchmark.sh
```

## Numdiff

For manual tests that require epsilon matching, [`numdiff`](https://www.nongnu.org/numdiff/) can be useful. Use it as follows:

```bash
numdiff --absolute-tolerance 0.0001 scratch/output-data/output.csv ~/graphs/pr-directed-test-PR
```
