package science.atlarge.graphalytics.umbra.test;

import org.junit.Ignore;
import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraLoadComputation;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.wcc.WeaklyConnectedComponentsComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class WeaklyConnectedComponentsTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement);
        c.execute();
    }

    @Test
    public void testDirectedWccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-wcc-directed", true, false);
        umbraLoadComputation.load();
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement);
        c.execute();
    }

    @Test
    public void testUndirectedWccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-wcc-undirected", false, false);
        umbraLoadComputation.load();
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement);
        c.execute();
    }

}
