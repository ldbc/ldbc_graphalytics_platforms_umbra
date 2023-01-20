#!/usr/bin/env python3

import umbra
import shutil
import time

args = umbra.parse_arguments()
pg_con = umbra.connect()
con = pg_con.cursor()

print(f"Processing starts at: {time.time_ns() // 1000000}")

# some ideas for implementing SSSP:
# - http://aprogrammerwrites.eu/?p=1391
# - http://aprogrammerwrites.eu/?p=1415
# - https://learnsql.com/blog/get-to-know-the-power-of-sql-recursive-queries/

con.execute("DROP TABLE IF EXISTS d")
con.execute("DROP TABLE IF EXISTS d2")

con.execute(f"""
    CREATE TABLE d AS
        SELECT {args.source_vertex} AS id, CAST(0 AS float) AS dist
    """)
con.execute(f"SELECT * FROM d")

# add 0-length loop edges
con.execute(f"INSERT INTO e SELECT id, id, 0.0 FROM v")

while True:
    con.execute(f"""
        CREATE TABLE d2 AS
            SELECT e.target AS id, min(d.dist + e.weight) AS dist
            FROM d
            JOIN e
            ON d.id = e.source
            GROUP BY e.target
        """)

    con.execute("""
        SELECT count(id) AS numchanged FROM (
            (
                SELECT id, dist FROM d
                EXCEPT
                SELECT id, dist FROM d2
            )
            UNION ALL
            (
                SELECT id, dist FROM d2
                EXCEPT
                SELECT id, dist FROM d
            )
        ) sub
        """)
    numchanged = con.fetchone()[0]

    con.execute("DROP TABLE d");
    con.execute("ALTER TABLE d2 RENAME TO d")

    if numchanged == 0:
        break

print(f"Processing ends at: {time.time_ns() // 1000000}")

# export results
con.execute(f"COPY (SELECT * FROM d ORDER BY id ASC) TO '{umbra.output_data_directory}/output.csv' (DELIMITER ' ')")

# move results to the place expected by the Graphalytics framework
shutil.move(f"{umbra.external_output_directory}/output.csv", f"{args.output_path}")
