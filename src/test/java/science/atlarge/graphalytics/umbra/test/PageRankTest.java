package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.bfs.BreadthFirstSearchComputation;
import science.atlarge.graphalytics.umbra.algorithms.pr.PageRankComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PageRankTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        TestGraphLoader.loadUndirected(statement);
        PageRankComputation c = new PageRankComputation(statement, 2, 0.85);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        TestGraphLoader.loadDirected(statement);
        PageRankComputation c = new PageRankComputation(statement, 2, 0.85);
        c.execute();
    }

}
