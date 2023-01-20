#!/usr/bin/env python3

import psycopg2
import argparse


input_data_directory = "/input-data/"
output_data_directory = "/output-data/"
# external output directory, relative to the execution path of the script
external_output_directory = 'scratch/output-data/'


def connect():
    pg_con = psycopg2.connect(host="localhost", user="postgres", password="mysecretpassword", database="ldbcsnb", port=5432)
    pg_con.autocommit = True
    return pg_con


def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument('--graph-name', type=str, required=True)
    parser.add_argument('--directed', type=bool)
    parser.add_argument('--weighted', type=bool)
    parser.add_argument('--algorithm', type=str)
    parser.add_argument('--source-vertex', type=int)
    parser.add_argument('--max-iteration', type=int)
    parser.add_argument('--damping-factor', type=float)
    parser.add_argument('--input-directory', type=str)

    # unused arguments
    parser.add_argument('--job-id', type=str)
    parser.add_argument('--log-path', type=str)
    parser.add_argument('--input-path', type=str)
    parser.add_argument('--output-path', type=str)
    parser.add_argument('--jobid', type=str)
    parser.add_argument('--dataset', type=str)
    parser.add_argument('--threadnum', type=int)

    args = parser.parse_args()
    return args
