#!/usr/bin/env python3

import umbra
import shutil
import time

args = umbra.parse_arguments()
pg_con = umbra.connect()
con = pg_con.cursor()

print(f"Processing starts at: {time.time_ns() // 1000000}")

con.execute("DROP TABLE IF EXISTS dangling")
con.execute("DROP TABLE IF EXISTS e_with_source_outdegrees")

for i in range(0, args.max_iteration+1):
    con.execute(f"DROP TABLE IF EXISTS pr{i}")
    con.execute(f"CREATE TABLE pr{i} (id INTEGER, value FLOAT)")

results = con.execute("SELECT count(*) AS n FROM v")
pr_n = con.fetchone()[0]

pr_teleport = (1-args.damping_factor)/pr_n
args.damping_factorangling_redistribution_factor = args.damping_factor/pr_n

con.execute(f"""
    CREATE TABLE dangling AS
    SELECT id FROM v WHERE NOT EXISTS (SELECT 1 FROM e WHERE source = id)
    """)

con.execute(f"""
    CREATE TABLE e_with_source_outdegrees AS
    SELECT e1.source AS source, e1.target AS target, count(e2.target) AS outdegree
    FROM e e1
    JOIN e e2
    ON e1.source = e2.source
    GROUP BY e1.source, e1.target
    """)

# initialize PR_0
con.execute(f"""
    INSERT INTO pr0
    SELECT id, 1.0/{pr_n} FROM v
    """)

# compute PR_1, ..., PR_#iterations
for i in range(1, args.max_iteration+1):
    con.execute(f"""
    INSERT INTO pr{i}
    SELECT
        v.id AS id,
        {pr_teleport} +
        {args.damping_factor} * coalesce(sum(pr{i-1}.value / e_with_source_outdegrees.outdegree), 0) +
        {args.damping_factorangling_redistribution_factor} * (SELECT coalesce(sum(pr{i-1}.value), 0) FROM pr{i-1} JOIN dangling ON pr{i-1}.id = dangling.id)
            AS value
    FROM v
    LEFT JOIN e_with_source_outdegrees
        ON e_with_source_outdegrees.target = v.id
    LEFT JOIN pr{i-1}
        ON pr{i-1}.id = e_with_source_outdegrees.source
    GROUP BY v.id
    """)
    con.execute(f"DROP TABLE pr{i-1}")

print(f"Processing ends at: {time.time_ns() // 1000000}")

# export results
con.execute(f"COPY (SELECT * FROM pr{args.max_iteration} ORDER BY id ASC) TO '{umbra.output_data_directory}/output.csv' (DELIMITER ' ')")

# move results to the place expected by the Graphalytics framework
shutil.move(f"{umbra.external_output_directory}/output.csv", f"{args.output_path}")
