package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraLoadComputation;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.bfs.BreadthFirstSearchComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BreadthFirstSearchTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        BreadthFirstSearchComputation breadthFirstSearchComputation = new BreadthFirstSearchComputation(statement, 2);
        breadthFirstSearchComputation.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        BreadthFirstSearchComputation breadthFirstSearchComputation = new BreadthFirstSearchComputation(statement, 1);
        breadthFirstSearchComputation.execute();
    }

    @Test
    public void testDirectedBfsTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-bfs-directed", true, false);
        umbraLoadComputation.load();
        BreadthFirstSearchComputation c = new BreadthFirstSearchComputation(statement, 1);
        c.execute();
    }

    @Test
    public void testUndirectedBfsTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-bfs-undirected", false, false);
        umbraLoadComputation.load();
        BreadthFirstSearchComputation c = new BreadthFirstSearchComputation(statement, 1);
        c.execute();
    }
}
