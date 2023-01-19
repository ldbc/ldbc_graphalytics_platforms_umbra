package science.atlarge.graphalytics.umbra;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;


/**
 * Base class for graph loading in the platform driver.
 */
public class UmbraLoader {

	private static final Logger LOG = LogManager.getLogger();

	protected CommandLine commandLine;
	protected FormattedGraph formattedGraph;
	protected UmbraConfiguration platformConfig;

	/**
	 * Graph loader for Umbra.
	 * @param formattedGraph
	 * @param platformConfig
	 */
	public UmbraLoader(FormattedGraph formattedGraph, UmbraConfiguration platformConfig) {
		this.formattedGraph = formattedGraph;
		this.platformConfig = platformConfig;
	}

	public int load(String loadedInputPath) throws Exception {
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

		String dataDirectory = "/data/";
		String graphPathWithoutExtension = dataDirectory + formattedGraph.getGraph().getName();

		// set schema strings based on whether the graph is weighted
		boolean weighted = formattedGraph.getGraph().getSourceGraph().hasEdgeProperties();
		String weightAttributeWithoutType = weighted ? ", weight"        : "";
		String weightAttributeWithType    = weighted ? ", weight FLOAT" : "";

		// cleanup
		statement.executeUpdate(String.format("DROP VIEW  IF EXISTS u;"));
		statement.executeUpdate(String.format("DROP TABLE IF EXISTS u;"));
		statement.executeUpdate(String.format("DROP TABLE IF EXISTS v;"));
		statement.executeUpdate(String.format("DROP TABLE IF EXISTS e;"));

		// create tables
		statement.executeUpdate(String.format("CREATE TABLE v (id INTEGER);"));
		statement.executeUpdate(String.format("CREATE TABLE e (source INTEGER, target INTEGER%s);", weightAttributeWithType));

		// load tables
		statement.executeUpdate(String.format("COPY v (id) FROM '%s.v' (DELIMITER ' ', FORMAT csv)", graphPathWithoutExtension));
		statement.executeUpdate(String.format("COPY e (source, target%s) FROM '%s.e' (DELIMITER ' ', FORMAT csv)", weightAttributeWithoutType, graphPathWithoutExtension));

		// create undirected variant
		if (formattedGraph.isDirected()) {
			statement.executeUpdate(String.format("CREATE TABLE u (source INTEGER, target INTEGER);"));
			statement.executeUpdate(String.format("INSERT INTO u SELECT target, source FROM e;"));
			statement.executeUpdate(String.format("INSERT INTO u SELECT source, target FROM e;"));
		} else {
			statement.executeUpdate(String.format("INSERT INTO e SELECT target, source%s FROM e;", weightAttributeWithoutType));
			statement.executeUpdate(String.format("CREATE VIEW u AS SELECT source, target FROM e;"));
		}

		return 0;
	}

	public int unload(String loadedInputPath) throws Exception {
		String unloaderDir = platformConfig.getUnloaderPath();
		commandLine = new CommandLine(Paths.get(unloaderDir).toFile());

		commandLine.addArgument("--graph-name");
		commandLine.addArgument(formattedGraph.getName());

		commandLine.addArgument("--output-path");
		commandLine.addArgument(loadedInputPath);

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Execute graph unloader with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);

		return executor.execute(commandLine);
	}

}
