#!/usr/bin/env python3

import umbra
import random
import shutil
import time

args = umbra.parse_arguments()
pg_con = umbra.connect()
con = pg_con.cursor()

# based on paper "In-database connected component analysis", https://arxiv.org/pdf/1802.09478.pdf

con.execute("DROP TABLE IF EXISTS tmp")
con.execute("DROP TABLE IF EXISTS ccgraph")
con.execute("DROP TABLE IF EXISTS ccgraph2")
con.execute("DROP TABLE IF EXISTS ccgraph3")

print(f"Processing starts at: {time.time_ns() // 1000000}")

con.execute("""
    CREATE TABLE ccgraph AS
        SELECT source AS v1, target AS v2
        FROM u
    """)

roundno = 0
stackA = []
stackB = []

while True:
    roundno += 1
    ccreps = f"ccreps{roundno}"

    rA = 0
    while rA == 0:
        rA = random.randint(-2**63, 2**63-1)

    rB = random.randint(-2**63, 2**63-1)
    stackA.append(rA)
    stackB.append(rB)

    con.execute(f"""
        CREATE TABLE {ccreps} AS
            SELECT
                v1 v,
                least(
                    axplusb({rA}, v1, {rB}),
                    min(axplusb({rA}, v2, {rB}))
                ) rep
            FROM ccgraph
            GROUP BY v1
        """)

    con.execute(f"""
        CREATE TABLE ccgraph2 AS
            SELECT r1.rep AS v1, v2
            FROM ccgraph, {ccreps} AS r1
            WHERE ccgraph.v1 = r1.v
        """)

    con.execute("DROP TABLE ccgraph")
    con.execute(f"""
        CREATE TABLE ccgraph3 AS
            SELECT DISTINCT v1, r2.rep AS v2
            FROM ccgraph2, {ccreps} AS r2
            WHERE ccgraph2.v2 = r2.v
            AND v1 != r2.rep
        """)

    con.execute("SELECT count(*) AS count FROM ccgraph3")
    graphsize = con.fetchone()[0]
    con.execute("DROP TABLE ccgraph2")
    con.execute("ALTER TABLE ccgraph3 RENAME TO ccgraph")

    if graphsize == 0:
        break

accA = 1
accB = 0

while True:
    roundno -= 1
    con.execute(f"SELECT axplusb({accA}, {stackA.pop()}, 0) AS accA")
    accA = con.fetchone()[0]

    con.execute(f"SELECT axplusb({accA}, {stackB.pop()}, {accB}) AS accB")
    accB = con.fetchone()[0]

    if roundno == 0:
        break
    ccrepsr = f"ccreps{roundno}"
    ccrepsr1 = f"ccreps{roundno+1}"
    con.execute(f"""
        CREATE TABLE tmp AS
            SELECT
                r1.v AS v,
                coalesce(r2.rep, axplusb({accA}, r1.rep, {accB})) AS rep
            FROM {ccrepsr} AS r1
            LEFT OUTER JOIN {ccrepsr1} AS r2
                        ON r1.rep = r2.v
        """)
    con.execute(f"DROP TABLE {ccrepsr}")
    con.execute(f"DROP TABLE {ccrepsr1}")
    con.execute(f"ALTER TABLE tmp RENAME TO {ccrepsr}")

con.execute("ALTER TABLE ccreps1 RENAME TO ccresult")
con.execute("DROP TABLE ccgraph")

print(f"Processing ends at: {time.time_ns() // 1000000}")

# export results
con.execute(f"COPY (SELECT * FROM ccresult ORDER BY v ASC) TO '{umbra.output_data_directory}/output.csv' (DELIMITER ' ')")

# move results to the place expected by the Graphalytics framework
shutil.move(f"{umbra.external_output_directory}/output.csv", f"{args.output_path}")
