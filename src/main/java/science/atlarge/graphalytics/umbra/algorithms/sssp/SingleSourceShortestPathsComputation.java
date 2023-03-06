package science.atlarge.graphalytics.umbra.algorithms.sssp;

import science.atlarge.graphalytics.umbra.UmbraComputation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SingleSourceShortestPathsComputation extends UmbraComputation {

    protected long sourceVertex;

    public SingleSourceShortestPathsComputation(Statement statement, long sourceVertex) {
        super(statement);
        this.sourceVertex = sourceVertex;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS d");
        statement.executeUpdate("DROP TABLE IF EXISTS d2");
        statement.executeUpdate("DROP TABLE IF EXISTS sssp");
    }

    @Override
    public void compute() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate(String.format(
                "CREATE TABLE d AS\n" +
                        "    SELECT %d AS id, CAST(0 AS float) AS dist",
                sourceVertex
        ));

        statement.executeUpdate("INSERT INTO e SELECT id, id, 0.0 FROM v");

        while (true) {
            statement.executeUpdate(
                    "CREATE TABLE d2 AS\n" +
                            "    SELECT e.target AS id, min(d.dist + e.weight) AS dist\n" +
                            "    FROM d\n" +
                            "    JOIN e\n" +
                            "      ON d.id = e.source\n" +
                            "    GROUP BY e.target"
            );
            ResultSet resultSet = statement.executeQuery(
                    "SELECT count(id) AS numChanged FROM (\n" +
                            "    (\n" +
                            "        SELECT id, dist FROM d\n" +
                            "        EXCEPT\n" +
                            "        SELECT id, dist FROM d2\n" +
                            "    )\n" +
                            "    UNION ALL\n" +
                            "    (\n" +
                            "        SELECT id, dist FROM d2\n" +
                            "        EXCEPT\n" +
                            "        SELECT id, dist FROM d\n" +
                            "    )\n" +
                            ") sub"
            );
            resultSet.next();
            long numChanged = resultSet.getLong(1);

            statement.executeUpdate("DROP TABLE d");
            statement.executeUpdate("ALTER TABLE d2 RENAME TO d");

            if (numChanged == 0) {
                break;
            }
        }

        statement.executeUpdate(
                "CREATE TABLE sssp AS " +
                        "  SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist " +
                        "  FROM v " +
                        "  LEFT JOIN d ON d.id = v.id"
        );

        // cleanup loop edges
        statement.executeUpdate("DELETE FROM e WHERE source = target AND weight = 0.0");

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate("COPY (SELECT * FROM sssp ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')");
    }

}
