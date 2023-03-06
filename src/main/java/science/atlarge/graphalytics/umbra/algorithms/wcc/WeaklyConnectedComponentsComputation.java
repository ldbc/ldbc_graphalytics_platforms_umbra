package science.atlarge.graphalytics.umbra.algorithms.wcc;

import org.apache.commons.lang.NotImplementedException;
import science.atlarge.graphalytics.umbra.UmbraComputation;

import java.sql.SQLException;
import java.sql.Statement;

public class WeaklyConnectedComponentsComputation extends UmbraComputation {

    public WeaklyConnectedComponentsComputation(Statement statement) {
        super(statement);
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS wcc");
    }

    @Override
    public void compute() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate("CREATE TABLE wcc(id INTEGER, component INTEGER)");
        statement.executeUpdate("INSERT INTO wcc\n" +
                "WITH RECURSIVE paths(startVertex, endVertex, path) AS (\n" +
                "   SELECT -- define the path as the first e of the traversal\n" +
                "        id AS startVertex,\n" +
                "        id AS endVertex,\n" +
                "        array[id, id] AS path\n" +
                "     FROM v\n" +
                "   UNION ALL\n" +
                "   SELECT -- concatenate new u to the path\n" +
                "        paths.startVertex AS startVertex,\n" +
                "        u.target AS endVertex,\n" +
                "        array_append(paths.path, u.target) AS path\n" +
                "     FROM paths\n" +
                "     JOIN u ON paths.endVertex = u.source\n" +
                "    -- Prevent adding a repeated v to the path.\n" +
                "    -- This ensures that no cycles occur.\n" +
                "    WHERE target != ALL(paths.path)\n" +
                ")\n" +
                "SELECT startVertex AS id, min(p) AS component\n" +
                "FROM (\n" +
                "  SELECT startVertex, endVertex, unnest(path) AS p\n" +
                "  FROM paths\n" +
                ") sub\n" +
                "GROUP BY startVertex\n" +
                "ORDER BY startVertex\n" +
                ";\n");

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate("COPY (SELECT * FROM wcc ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')");
    }

}
