# Postgres/Umbra Graphalytics implementation

Implementation of LDBC Graphalytics using [PostgreSQL](https://www.postgresql.org/) and [Umbra](https://umbra-db.com/).

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

Run the following command:

```bash
scripts/package-and-run-benchmark.sh
```

## Numdiff

For manual tests that require epsilon matching, [`numdiff`](https://www.nongnu.org/numdiff/) can be useful. Use it as follows:

```bash
numdiff --absolute-tolerance 0.0001 scratch/output-data/output.csv ~/graphs/pr-directed-test-PR
```
