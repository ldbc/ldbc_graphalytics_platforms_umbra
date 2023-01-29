package science.atlarge.graphalytics.umbra.algorithms.bfs;

import science.atlarge.graphalytics.umbra.UmbraComputation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BreadthFirstSearchComputation extends UmbraComputation {

    protected long sourceVertex;

    public BreadthFirstSearchComputation(Statement statement, long sourceVertex) {
        super(statement);
        this.sourceVertex = sourceVertex;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS bfs");
        statement.executeUpdate("DROP TABLE IF EXISTS frontier");
        statement.executeUpdate("DROP TABLE IF EXISTS next");
        statement.executeUpdate("DROP TABLE IF EXISTS seen");
    }

    @Override
    public void compute() throws SQLException {

        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate("CREATE TABLE frontier(id INTEGER)");
        statement.executeUpdate("CREATE TABLE next(id INTEGER)");
        statement.executeUpdate("CREATE TABLE seen(id INTEGER, level INTEGER)");

        int level = 0;
        statement.executeUpdate(String.format("INSERT INTO next VALUES (%d)", sourceVertex));
        statement.executeUpdate(String.format("INSERT INTO seen (SELECT id, %d FROM next)", level));
        statement.executeUpdate("DELETE FROM frontier");
        statement.executeUpdate("INSERT INTO frontier (SELECT * FROM next)");
        statement.executeUpdate("DELETE FROM next");

        while (true) {
            level++;
            statement.executeUpdate("INSERT INTO next " +
                    "SELECT DISTINCT e.target " +
                    "  FROM frontier JOIN e ON e.source = frontier.id " +
                    " WHERE NOT EXISTS (SELECT 1 FROM seen WHERE id = e.target)"
            );

            ResultSet resultSet = statement.executeQuery("SELECT count(id) AS count FROM next");
            resultSet.next();
            long count = resultSet.getLong(1);
            if (count == 0) {
                break;
            }

            statement.executeUpdate(String.format("INSERT INTO seen (SELECT id, %d FROM next)", level));
            statement.executeUpdate("DELETE FROM frontier");
            statement.executeUpdate("INSERT INTO frontier (SELECT * FROM next)");
            statement.executeUpdate("DELETE FROM next");
        }

        statement.executeUpdate(
                "CREATE TABLE bfs AS " +
                        "  SELECT v.id, coalesce(seen.level, 9223372036854775807) AS level " +
                        "  FROM v " +
                        "  LEFT JOIN seen ON seen.id = v.id"
        );

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate("COPY (SELECT * FROM bfs ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')");
    }

}
