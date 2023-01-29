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
        throw new NotImplementedException();
    }

    @Override
    public void compute() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate("COPY (SELECT * FROM wcc ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')");

        throw new NotImplementedException();
    }

}
