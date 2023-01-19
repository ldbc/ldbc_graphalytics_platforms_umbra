import java.sql.*;
import java.util.Properties;

public class MyMain {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.ds.PGSimpleDataSource");

        Properties props = new Properties();
        String endPoint = "jdbc:postgresql://localhost:5432/";
        String databaseName = "ldbcsnb";
        String password = "mysecretpassword";
        String userName = "postgres";
        props.setProperty("jdbcUrl", endPoint);
        props.setProperty("dataSource.databaseName", databaseName);
        props.setProperty("dataSource.assumeMinServerVersion", "9.0");
        props.setProperty("dataSource.ssl", "false");
        props.setProperty("user", userName);
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(endPoint, props);

        Statement statement = conn.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS v;");
        statement.executeUpdate("DROP TABLE IF EXISTS e;");
        statement.executeUpdate("CREATE TABLE v (id INTEGER);");
        statement.executeUpdate("CREATE TABLE e (source INTEGER, target INTEGER, weight FLOAT);");
        statement.executeUpdate("COPY v FROM '/data/example-directed.v' (DELIMITER ' ', FORMAT csv);");
        statement.executeUpdate("COPY e (source, target, weight) FROM '/data/example-directed.e' (DELIMITER ' ', FORMAT csv);");
        ResultSet resultSet = statement.executeQuery("SELECT count(*) AS cv FROM v;");
        resultSet.next();
        long cv = resultSet.getLong(1);
        System.out.println(cv);
    }
}
