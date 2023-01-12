import duckdb
import random
import os


def init_tables(con, graph, directed):
    ## graph tables
    con.execute("CREATE TABLE v (id INTEGER)")
    con.execute("CREATE TABLE e (source INTEGER, target INTEGER, value DOUBLE)")

    ## loading
    con.execute(f"COPY v (id) FROM '{graph}.v' (DELIMITER ' ', FORMAT csv)")
    con.execute(f"COPY e (source, target, value) FROM '{graph}.e' (DELIMITER ' ', FORMAT csv)")

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


def bfs(con, bfs_source):
    con.execute("CREATE TABLE frontier(id INTEGER)")
    con.execute("CREATE TABLE next(id INTEGER)")
    con.execute("CREATE TABLE seen(id INTEGER, level INTEGER)")

    # initial node
    level = 0
    con.execute(f"INSERT INTO next VALUES ({bfs_source})")
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

    con.execute(f"SELECT * FROM seen")
    results = con.fetchall()
    for result in results:
        print(result)


def cdlp(con, cdlp_iterations):
    for i in range(0, cdlp_iterations+1):
        con.execute(f"CREATE TABLE cdlp{i} (id INTEGER, label INTEGER)")

    print("========================================")
    print("CDLP")
    print("========================================")
    con.execute("""
        INSERT INTO cdlp0
        SELECT id, id
        FROM v
        """)

    # We select the minimum mode value (the smallest one from the most frequent labels).
    # We use the cdlp{i-1} table to compute cdlp{i}, then throw away the cdlp{i-1} table.
    for i in range(1, cdlp_iterations+1):
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

    con.execute(f"SELECT * FROM cdlp{cdlp_iterations}")
    results = con.fetchall()
    for result in results:
        print(result)

    # TODO: CDLP directed is incorrect


def lcc(con):
    print("========================================")
    print("LCC")
    print("========================================")
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
    results = con.fetchall()
    for result in results:
        print(result)


def pr(con, pr_iterations):
    for i in range(0, pr_iterations+1):
        con.execute(f"CREATE TABLE pr{i} (id INTEGER, value DOUBLE)")
    print("========================================")
    print("PR")
    print("========================================")

    results = con.execute("SELECT count(*) AS n FROM v")
    pr_n = con.fetchone()[0]

    pr_teleport = (1-pr_d)/pr_n
    pr_dangling_redistribution_factor = pr_d/pr_n

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
    for i in range(1, pr_iterations+1):
        con.execute(f"""
        INSERT INTO pr{i}
        SELECT
            v.id AS id,
            {pr_teleport} +
            {pr_d} * coalesce(sum(pr{i-1}.value / e_with_source_outdegrees.outdegree), 0) +
            {pr_dangling_redistribution_factor} * (SELECT coalesce(sum(pr{i-1}.value), 0) FROM pr{i-1} JOIN dangling ON pr{i-1}.id = dangling.id)
                AS value
        FROM v
        LEFT JOIN e_with_source_outdegrees
            ON e_with_source_outdegrees.target = v.id
        LEFT JOIN pr{i-1}
            ON pr{i-1}.id = e_with_source_outdegrees.source
        GROUP BY v.id
        """)
        con.execute(f"DROP TABLE pr{i-1}")

    con.execute(f"SELECT * FROM pr{pr_iterations}")
    results = con.fetchall()
    for result in results:
        print(result)


def sssp(con, sssp_source):
    print("========================================")
    print("SSSP")
    print("========================================")
    # http://aprogrammerwrites.eu/?p=1391
    # http://aprogrammerwrites.eu/?p=1415
    # https://learnsql.com/blog/get-to-know-the-power-of-sql-recursive-queries/

    con.execute(f"""
        CREATE TABLE d AS
            SELECT {sssp_source} AS id, CAST(0 AS float) AS dist
        """)
    con.execute(f"SELECT * FROM d")

    # add 0-length loop edges
    con.execute(f"INSERT INTO e SELECT id, id, 0.0 FROM v")

    while True:
        con.execute(f"""
            CREATE TABLE d2 AS
                SELECT e.target AS id, min(d.dist + e.value) AS dist
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
            )
            """)
        numchanged = con.fetchone()[0]

        con.execute("DROP TABLE d");
        con.execute("ALTER TABLE d2 RENAME TO d")

        if numchanged == 0:
            break

    con.execute(f"SELECT * FROM d")
    results = con.fetchall()
    for result in results:
        print(result)


def wcc(con):
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
    graph = "graphs/example-undirected"
    bfs_source = 2
    sssp_source = 2

## TODO: handle unweighted edges

print("init")
init_tables(con, graph, directed)
print("asdf")

