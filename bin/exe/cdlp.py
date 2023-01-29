#!/usr/bin/env python3

import umbra
import shutil
import time

args = umbra.parse_arguments()
pg_con = umbra.connect()
con = pg_con.cursor()

print(f"Processing starts at: {time.time_ns() // 1000000}")

for i in range(0, args.max_iteration+1):
    con.execute(f"DROP TABLE IF EXISTS cdlp{i}")
    con.execute(f"CREATE TABLE cdlp{i} (id INTEGER, label INTEGER)")

con.execute("""
    INSERT INTO cdlp0
    SELECT id, id
    FROM v
    """)

# We select the minimum mode value (the smallest one from the most frequent labels).
# We use the cdlp{i-1} table to compute cdlp{i}, then throw away the cdlp{i-1} table.
for i in range(1, args.max_iteration+1):
    con.execute(f"""
    INSERT INTO cdlp{i}
    SELECT id, label FROM (
        SELECT
            u.source AS id,
            cdlp{i-1}.label AS label,
            ROW_NUMBER() OVER (PARTITION BY u.source ORDER BY count(*) DESC, cdlp{i-1}.label ASC) AS seqnum
        FROM u
        LEFT JOIN cdlp{i-1}
        ON cdlp{i-1}.id = u.target
        GROUP BY
            u.source,
            cdlp{i-1}.label
        ) most_frequent_labels
    WHERE seqnum = 1
    ORDER BY id
    """)
    con.execute(f"DROP TABLE cdlp{i-1}")

print(f"Processing ends at: {time.time_ns() // 1000000}")

# export results
con.execute(f"COPY (SELECT * FROM cdlp{args.max_iteration} ORDER BY id ASC) TO '{umbra.output_data_directory}/output.csv' (DELIMITER ' ')")

# move results to the place expected by the Graphalytics framework
shutil.move(f"{umbra.external_output_directory}/output.csv", f"{args.output_path}")
