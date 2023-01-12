import duckdb
import random
import os

memory = True

if memory:
    con = duckdb.connect(database=":memory:")
else:
    dbfile = "test.duckdb"
    if os.path.exists(dbfile):
        os.remove(dbfile)
    con = duckdb.connect(database=dbfile)

directed = False

pr_d = 0.85
pr_iterations = 2
cdlp_iterations = 2

## set data set
if directed:
    graph = "graphs/example-directed"
    bfs_source = 1
    sssp_source = 1
else:
    graph = "/Users/szarnyasg/graphs/dota-league"
    bfs_source = 2
    sssp_source = 2

## TODO: handle unweighted edges

## graph tables
con.execute("CREATE TABLE v (id INTEGER)")
con.execute("CREATE TABLE e (source INTEGER, target INTEGER, value DOUBLE)")

## loading
con.execute(f"COPY v (id) FROM '{graph}.v' (DELIMITER ' ', FORMAT csv)")
con.execute(f"COPY e (source, target, value) FROM '{graph}.e' (DELIMITER ' ', FORMAT csv)")

## auxiliary tables
### CDLP
for i in range(0, cdlp_iterations+1):
    con.execute(f"CREATE TABLE cdlp{i} (id INTEGER, label INTEGER)")
### PR
for i in range(0, pr_iterations+1):
    con.execute(f"CREATE TABLE pr{i} (id INTEGER, value DOUBLE)")
### BFS
con.execute("CREATE TABLE frontier(id INTEGER)")
con.execute("CREATE TABLE next(id INTEGER)")
con.execute("CREATE TABLE seen(id INTEGER, level INTEGER)")

# create undirected variant:
# - for directed graphs, it is an actual table
# - for undirected ones, it is just a view on table e
if directed:
    con.execute(f"CREATE TABLE u (target INTEGER, source INTEGER, value INTEGER)")
    con.execute(f"COPY u (target, source, value) FROM '{graph}.e' (DELIMITER ' ', FORMAT csv)")
    con.execute(f"COPY u (source, target, value) FROM '{graph}.e' (DELIMITER ' ', FORMAT csv)")
else:
    con.execute(f"COPY e (target, source, value) FROM '{graph}.e' (DELIMITER ' ', FORMAT csv)")
    con.execute(f"CREATE VIEW u AS SELECT source, target, value FROM e")





###################################################################################################################
###################################################################################################################
###################################################################################################################
print("========================================")
print("WCC")
print("========================================")
# based on paper "In-database connected component analysis", https://arxiv.org/pdf/1802.09478.pdf

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

con.execute(f"SELECT * FROM ccresult")
results = con.fetchall()
for result in results:
    print(result)
