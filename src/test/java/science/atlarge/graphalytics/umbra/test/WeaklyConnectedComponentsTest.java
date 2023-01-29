package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.bfs.BreadthFirstSearchComputation;
import science.atlarge.graphalytics.umbra.algorithms.wcc.WeaklyConnectedComponentsComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class WeaklyConnectedComponentsTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        TestGraphLoader.loadUndirected(statement);
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        TestGraphLoader.loadDirected(statement);
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement);
        c.execute();
    }

}
