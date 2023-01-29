package science.atlarge.graphalytics.umbra.algorithms.cdlp;

import science.atlarge.graphalytics.domain.algorithms.CommunityDetectionLPParameters;
import science.atlarge.graphalytics.umbra.UmbraComputation;

import java.sql.SQLException;
import java.sql.Statement;

public class CommunityDetectionLPComputation extends UmbraComputation {

    protected int numIterations;

    public CommunityDetectionLPComputation(Statement statement, int numIterations) {
        super(statement);
        this.numIterations = numIterations;
    }

    @Override
    public void cleanup() throws SQLException {
        for (int i = 0; i <= numIterations; i++) {
            statement.executeUpdate(String.format("DROP TABLE IF EXISTS cdlp%d", i));
        }
    }

    @Override
    public void compute() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        for (int i = 0; i <= numIterations; i++) {
            statement.executeUpdate(String.format("CREATE TABLE cdlp%d (id INTEGER, label INTEGER)", i));
        }

        statement.executeUpdate("INSERT INTO cdlp0\n" +
                "    SELECT id, id\n" +
                "    FROM v");

        // We select the minimum mode value (the smallest one from the most frequent labels).
        // We use the cdlp{i-1} table to compute cdlp{i}, then throw away the cdlp{i-1} table.
        for (int i = 1; i <= numIterations; i++) {
            statement.executeUpdate(String.format("INSERT INTO cdlp%d\n" +
                            "    SELECT id, label FROM (\n" +
                            "        SELECT\n" +
                            "            u.source AS id,\n" +
                            "            cdlp%d.label AS label,\n" +
                            "            ROW_NUMBER() OVER (PARTITION BY u.source ORDER BY count(*) DESC, cdlp%d.label ASC) AS seqnum\n" +
                            "        FROM u\n" +
                            "        LEFT JOIN cdlp%d\n" +
                            "               ON cdlp%d.id = u.target\n" +
                            "        GROUP BY\n" +
                            "            u.source,\n" +
                            "            cdlp%d.label\n" +
                            "        ) most_frequent_labels\n" +
                            "    WHERE seqnum = 1\n" +
                            "    ORDER BY id",
                    i, i-1, i-1, i-1, i-1, i-1)
            );
        }

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate(String.format("COPY (SELECT * FROM cdlp%d ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')", numIterations));
    }

}
