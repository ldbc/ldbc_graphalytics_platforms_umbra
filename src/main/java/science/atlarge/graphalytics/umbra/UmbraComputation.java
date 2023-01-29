package science.atlarge.graphalytics.umbra;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class UmbraComputation {

    protected final Statement statement;
    protected final Logger LOG = LogManager.getLogger();


    public UmbraComputation(Statement statement) {
        this.statement = statement;
    }

    public abstract void cleanup() throws SQLException;

    public abstract void compute() throws SQLException;

    public void execute() throws SQLException {
        cleanup();
        compute();
        cleanup();
    }

}
