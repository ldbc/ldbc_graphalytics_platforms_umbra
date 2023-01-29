package science.atlarge.graphalytics.umbra.algorithms.pr;

import science.atlarge.graphalytics.umbra.UmbraComputation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class PageRankComputation extends UmbraComputation {

    protected int maxIterations;
    protected double dampingFactor;

    public PageRankComputation(Statement statement, int maxIterations, double dampingFactor) {
        super(statement);
        this.maxIterations = maxIterations;
        this.dampingFactor = dampingFactor;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS dangling");
        statement.executeUpdate("DROP TABLE IF EXISTS e_with_source_outdegrees");
    }

    @Override
    public void compute() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        for (int i = 0; i <= maxIterations; i++) {
            statement.executeUpdate(String.format("DROP TABLE IF EXISTS pr%d", i));
            statement.executeUpdate(String.format("CREATE TABLE pr%d (id INTEGER, value FLOAT)", i));
        }

        ResultSet resultSet = statement.executeQuery("SELECT count(*) AS n FROM v");
        resultSet.next();
        long prN = resultSet.getLong(1);
        double prTeleport = (1 - dampingFactor)/ prN;
        double redistributionFactor = dampingFactor / prN;

        statement.executeUpdate(
                "CREATE TABLE dangling AS\n" +
                        "    SELECT id FROM v WHERE NOT EXISTS (SELECT 1 FROM e WHERE source = id)"
        );

        statement.executeUpdate(
                "CREATE TABLE e_with_source_outdegrees AS\n" +
                        "    SELECT e1.source AS source, e1.target AS target, count(e2.target) AS outdegree\n" +
                        "    FROM e e1\n" +
                        "    JOIN e e2\n" +
                        "      ON e1.source = e2.source\n" +
                        "    GROUP BY e1.source, e1.target"
        );
        // initialize PR_0
        statement.executeUpdate(String.format(
                "INSERT INTO pr0\n" +
                        "    SELECT id, 1.0/%d FROM v",
                prN
        ));

        // compute PR_1, ..., PR_#iterations
        for (int i = 1; i <= maxIterations; i++) {
            statement.executeUpdate(String.format(Locale.ROOT,
                    "INSERT INTO pr%d\n" +
                            "    SELECT\n" +
                            "        v.id AS id,\n" +
                            "        %f +\n" +
                            "        %f * coalesce(sum(pr%d.value / e_with_source_outdegrees.outdegree), 0) +\n" +
                            "        %f * (SELECT coalesce(sum(pr%d.value), 0) FROM pr%d JOIN dangling ON pr%d.id = dangling.id)\n" +
                            "            AS value\n" +
                            "    FROM v\n" +
                            "    LEFT JOIN e_with_source_outdegrees\n" +
                            "           ON e_with_source_outdegrees.target = v.id\n" +
                            "    LEFT JOIN pr%d\n" +
                            "           ON pr%d.id = e_with_source_outdegrees.source\n" +
                            "    GROUP BY v.id\n",
                    i, prTeleport, dampingFactor, i - 1, redistributionFactor,
                    i - 1, i - 1, i - 1, i - 1, i - 1
            ));
            statement.executeUpdate(String.format("DROP TABLE pr%d", i - 1));
        }

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate(String.format("COPY (SELECT * FROM pr%d ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')", maxIterations));
    }
}
