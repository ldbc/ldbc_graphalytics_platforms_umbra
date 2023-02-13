# Postgres/Umbra Graphalytics implementation

Implementation of LDBC Graphalytics using PostgreSQL and [Umbra](https://umbra-db.com/):

Pointers:

* [Graphalytics specification](https://ldbcouncil.org/ldbc_graphalytics_docs/graphalytics_spec.pdf)
* [Graphalytics website](https://ldbcouncil.org/benchmarks/graphalytics/)

## Getting started

To set up a Postgres instance, run e.g.:

```bash
export POSTGRES_INPUT_DATA_DIR=~/graphs
bin/scripts/start-postgres.sh
```

## Testing

To test the current state of your codebase, create a new commit:

```bash
git commit -am "WIP"
```

Then, run the following command.

:warning: This script amends the last commit, so ensure that you have a `WIP` commit on top of your commit tree.

```bash
scripts/package-and-run-benchmark.sh
```

## Numdiff

For manual tests that require epsilon matching, [`numdiff`](https://www.nongnu.org/numdiff/) can be useful. Use it as follows:

```bash
numdiff --absolute-tolerance 0.0001 scratch/output-data/output.csv ~/graphs/pr-directed-test-PR
```


