package science.atlarge.graphalytics.umbra.algorithms.lcc;

import science.atlarge.graphalytics.umbra.UmbraComputation;

import java.sql.SQLException;
import java.sql.Statement;

public class LocalClusteringCoefficientComputation extends UmbraComputation {

    public LocalClusteringCoefficientComputation(Statement statement) {
        super(statement);
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS lcc");
        statement.executeUpdate("DROP VIEW IF EXISTS neighbors");
    }

    @Override
    public void compute() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate("CREATE VIEW neighbors AS (\n" +
                "\t\t\tSELECT e.source AS vertex, e.target AS neighbor\n" +
                "\t\t\tFROM e\n" +
                "\t\t\tUNION\n" +
                "\t\t\tSELECT e.target AS vertex, e.source AS neighbor\n" +
                "\t\t\tFROM e\n" +
                "\t\t\t)");
        statement.executeUpdate("CREATE TABLE lcc AS\n" +
                "SELECT\n" +
                "id,\n" +
                "CASE WHEN tri = 0 THEN 0.0 ELSE (CAST(tri AS float) / (deg*(deg-1))) END AS value\n" +
                "FROM (\n" +
                "    SELECT\n" +
                "        v.id AS id,\n" +
                "        (SELECT count(*) FROM neighbors WHERE neighbors.vertex = v.id) AS deg,\n" +
                "        (\n" +
                "            SELECT count(*)\n" +
                "            FROM neighbors n1\n" +
                "            JOIN neighbors n2\n" +
                "              ON n1.vertex = n2.vertex\n" +
                "            JOIN e e3\n" +
                "              ON e3.source = n1.neighbor\n" +
                "             AND e3.target = n2.neighbor\n" +
                "            WHERE n1.vertex = v.id\n" +
                "        ) AS tri\n" +
                "    FROM v\n" +
                "    ORDER BY v.id ASC\n" +
                ") s\n");

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate("COPY (SELECT * FROM lcc ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')");
    }

}
