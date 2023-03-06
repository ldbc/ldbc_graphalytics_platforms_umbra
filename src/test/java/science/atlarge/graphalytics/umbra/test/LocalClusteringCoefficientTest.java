package science.atlarge.graphalytics.umbra.test;

import org.junit.Test;
import science.atlarge.graphalytics.umbra.UmbraLoadComputation;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.lcc.LocalClusteringCoefficientComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalClusteringCoefficientTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement);
        c.execute();
    }

    @Test
    public void testDirectedLccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-lcc-directed", true, false);
        umbraLoadComputation.load();
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement);
        c.execute();
    }

    @Test
    public void testUndirectedLccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();
        UmbraLoadComputation umbraLoadComputation = new UmbraLoadComputation(
                statement, "test-lcc-undirected", false, false);
        umbraLoadComputation.load();
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement);
        c.execute();
    }

    @Test
    public void testFosdem() throws SQLException, ClassNotFoundException {
        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        TestGraphLoader.loadFosdem(statement);
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement);
        c.execute();
    }

}
