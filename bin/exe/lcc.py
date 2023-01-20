#!/usr/bin/env python3

import umbra
import shutil
import time

args = umbra.parse_arguments()
pg_con = umbra.connect()
con = pg_con.cursor()

print(f"Processing starts at: {time.time_ns() // 1000000}")

con.execute("DROP VIEW IF EXISTS neighbors")
con.execute("""CREATE VIEW neighbors AS (
    SELECT e.source AS vertex, e.target AS neighbor
    FROM e
    UNION
    SELECT e.target AS vertex, e.source AS neighbor
    FROM e
    )
""")
con.execute("""
SELECT
id,
CASE WHEN tri = 0 THEN 0.0 ELSE (CAST(tri AS float) / (deg*(deg-1))) END AS value
FROM (
    SELECT
        v.id AS id,
        (SELECT count(*) FROM neighbors WHERE neighbors.vertex = v.id) AS deg,
        (
            SELECT count(*)
            FROM neighbors n1
            JOIN neighbors n2
            ON n1.vertex = n2.vertex
            JOIN e e3
            ON e3.source = n1.neighbor
            AND e3.target = n2.neighbor
            WHERE n1.vertex = v.id
        ) AS tri
    FROM v
    ORDER BY v.id ASC
) s
""")

print(f"Processing ends at: {time.time_ns() // 1000000}")

# export results
con.execute(f"COPY (SELECT * FROM lcc ORDER BY id ASC) TO '{umbra.output_data_directory}/output.csv' (DELIMITER ' ')")

# move results to the place expected by the Graphalytics framework
shutil.move(f"{umbra.external_output_directory}/output.csv", f"{args.output_path}")
