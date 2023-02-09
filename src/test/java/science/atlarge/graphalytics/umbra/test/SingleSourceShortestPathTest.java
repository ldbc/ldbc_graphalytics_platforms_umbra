package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraLoadComputation;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.bfs.BreadthFirstSearchComputation;
import science.atlarge.graphalytics.umbra.algorithms.pr.PageRankComputation;
import science.atlarge.graphalytics.umbra.algorithms.sssp.SingleSourceShortestPathComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SingleSourceShortestPathTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        TestGraphLoader.loadUndirected(statement);
        SingleSourceShortestPathComputation c = new SingleSourceShortestPathComputation(statement, 2);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        TestGraphLoader.loadDirected(statement);
        SingleSourceShortestPathComputation c = new SingleSourceShortestPathComputation(statement, 1);
        c.execute();
    }

    @Test
    public void testDirectedSsspTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "sssp-directed-test", true, true);
        umbraLoadComputation.load();
        SingleSourceShortestPathComputation c = new SingleSourceShortestPathComputation(statement, 1);
        c.execute();
    }

    @Test
    public void testUndirectedSsspTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "sssp-undirected-test", false, true);
        umbraLoadComputation.load();
        SingleSourceShortestPathComputation c = new SingleSourceShortestPathComputation(statement, 1);
        c.execute();
    }

}
