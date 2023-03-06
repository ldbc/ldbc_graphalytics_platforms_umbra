package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraLoadComputation;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.pr.PageRankComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PageRankTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        PageRankComputation c = new PageRankComputation(statement, 2, 0.85);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        PageRankComputation c = new PageRankComputation(statement, 2, 0.85);
        c.execute();
    }

    @Test
    public void testPageRankDirectedGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-pr-directed", true, false);
        umbraLoadComputation.load();
        PageRankComputation c = new PageRankComputation(statement, 14, 0.85);
        c.execute();
    }

    @Test
    public void testPageRankUndirectedGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-pr-undirected", false, false);
        umbraLoadComputation.load();
        PageRankComputation c = new PageRankComputation(statement, 26, 0.85);
        c.execute();
    }

}
