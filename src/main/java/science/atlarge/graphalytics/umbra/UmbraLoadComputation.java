package science.atlarge.graphalytics.umbra;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UmbraLoadComputation {

    protected Statement statement;
    protected String name;
    protected boolean directed;
    protected boolean weighted;

    public UmbraLoadComputation(Statement statement, String name, boolean directed, boolean weighted) {
        this.statement = statement;
        this.name = name;
        this.directed = directed;
        this.weighted = weighted;
    }

    public void load() throws SQLException {
        unload();

        String weightAttributeWithoutType = "";
        String weightAttributeWithType = "";
        if (weighted) {
            weightAttributeWithoutType = ", weight";
            weightAttributeWithType = ", weight FLOAT";
        }

        statement.executeUpdate(String.format("CREATE TABLE v (id INTEGER)"));
        statement.executeUpdate(String.format("CREATE TABLE e (source INTEGER, target INTEGER%s)", weightAttributeWithType));

        String loaderConfiguration = "(DELIMITER ' ', FORMAT csv)";
        String dataDirectoryPath = "/input-data/";
        statement.executeUpdate(String.format(
                "COPY v (id) FROM '%s/%s.v' %s",
                dataDirectoryPath,
                name,
                loaderConfiguration
        ));
        statement.executeUpdate(String.format(
                "COPY e (source, target%s) FROM '%s/%s.e' (DELIMITER ' ', FORMAT csv)",
                weightAttributeWithoutType,
                dataDirectoryPath,
                name,
                loaderConfiguration
        ));

        // create undirected table 'u'
        // note that for undirected graphs, table 'e' only stores the edges one way
        if (directed) {
            statement.executeUpdate("CREATE TABLE u (source INTEGER, target INTEGER)");
            statement.executeUpdate("INSERT INTO u SELECT target, source FROM e");
            statement.executeUpdate("INSERT INTO u SELECT source, target FROM e");
        } else {
            // for undirected graphs, copy edges the other way around
            statement.executeUpdate(String.format("INSERT INTO e SELECT target, source%s FROM e", weightAttributeWithoutType));
            // then u is just a copy of e
            statement.executeUpdate("CREATE TABLE u AS SELECT source, target FROM e");
        }
    }

    public void unload() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS u CASCADE");
        statement.executeUpdate("DROP TABLE IF EXISTS v CASCADE");
        statement.executeUpdate("DROP TABLE IF EXISTS e CASCADE");
    }

}
