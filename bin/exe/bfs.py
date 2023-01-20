#!/usr/bin/env python3

import umbra
import shutil
import time

args = umbra.parse_arguments()
pg_con = umbra.connect()
con = pg_con.cursor()

con.execute("DROP TABLE IF EXISTS frontier")
con.execute("DROP TABLE IF EXISTS next")
con.execute("DROP TABLE IF EXISTS seen")
con.execute("DROP TABLE IF EXISTS bfs")

print(f"Processing starts at: {time.time_ns() // 1000000}")

con.execute("CREATE TABLE frontier(id INTEGER)")
con.execute("CREATE TABLE next(id INTEGER)")
con.execute("CREATE TABLE seen(id INTEGER, level INTEGER)")

# initial node
level = 0
con.execute(f"INSERT INTO next VALUES ({args.source_vertex})")
con.execute(f"INSERT INTO seen (SELECT id, {level} FROM next)")
con.execute(f"DELETE FROM frontier")
con.execute(f"INSERT INTO frontier (SELECT * FROM next)")
con.execute(f"DELETE FROM next")

while True:
    level = level + 1

    con.execute(f"""
        INSERT INTO next
        (SELECT DISTINCT e.target
        FROM frontier
        JOIN e
        ON e.source = frontier.id
        WHERE NOT EXISTS (SELECT 1 FROM seen WHERE id = e.target))
        """)

    con.execute(f"SELECT count(id) AS count FROM next")
    count = con.fetchone()[0]
    if count == 0:
        break

    con.execute(f"INSERT INTO seen (SELECT id, {level} FROM next)")
    con.execute(f"DELETE FROM frontier")
    con.execute(f"INSERT INTO frontier (SELECT * FROM next)")
    con.execute(f"DELETE FROM next")

con.execute(f"""
    CREATE TABLE bfs AS
        SELECT v.id, coalesce(seen.level, 9223372036854775807) AS level
        FROM v
        LEFT JOIN seen ON seen.id = v.id
    """)

print(f"Processing ends at: {time.time_ns() // 1000000}")

# export results
con.execute(f"COPY (SELECT * FROM bfs ORDER BY id ASC) TO '{umbra.output_data_directory}/output.csv' (DELIMITER ' ')")

# move results to the place expected by the Graphalytics framework
shutil.move(f"{umbra.external_output_directory}/output.csv", f"{args.output_path}")
