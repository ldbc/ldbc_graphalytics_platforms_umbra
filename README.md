# Umbra Graphalytics implementation

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

Then, run the following comment. :warning: This amend the last commit, so ensure that you have a `WIP` commit on top of your commit tree.

```bash
scripts/package-and-run-benchmark.sh
```
