package science.atlarge.graphalytics.umbra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class UmbraUtil {

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.ds.PGSimpleDataSource");

        Properties props = new Properties();
        String databaseName = "ldbcsnb";
        String endPoint = "jdbc:postgresql://localhost:5432/" + databaseName;
        String password = "mysecretpassword";
        String userName = "postgres";
        props.setProperty("jdbcUrl", endPoint);
        props.setProperty("dataSource.databaseName", databaseName);
        props.setProperty("dataSource.assumeMinServerVersion", "9.0");
        props.setProperty("dataSource.ssl", "false");
        props.setProperty("user", userName);
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(endPoint, props);
        return conn;
    }

}
