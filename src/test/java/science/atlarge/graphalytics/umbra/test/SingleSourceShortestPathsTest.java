package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraLoadComputation;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.lcc.LocalClusteringCoefficientComputation;
import science.atlarge.graphalytics.umbra.algorithms.sssp.SingleSourceShortestPathsComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SingleSourceShortestPathsTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(statement, 2);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(statement, 1);
        c.execute();
    }

    @Test
    public void testDirectedSsspTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-sssp-directed", true, true);
        umbraLoadComputation.load();
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(statement, 1);
        c.execute();
    }

    @Test
    public void testUndirectedSsspTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-sssp-undirected", false, true);
        umbraLoadComputation.load();
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(statement, 1);
        c.execute();
    }

    @Test
    public void testFosdem() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadFosdemWeighted(statement);
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(statement, 2);
        c.execute();
    }

}
