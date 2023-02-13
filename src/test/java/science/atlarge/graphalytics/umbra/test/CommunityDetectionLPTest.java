package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraLoadComputation;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.cdlp.CommunityDetectionLPComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CommunityDetectionLPTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, 2);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, 2);
        c.execute();
    }

    @Test
    public void testDirectedCdlpTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-cdlp-directed", true, false);
        umbraLoadComputation.load();
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, 5);
        c.execute();
    }

    @Test
    public void testUndirectedCdlpTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-cdlp-undirected", false, false);
        umbraLoadComputation.load();
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, 5);
        c.execute();
    }

}
