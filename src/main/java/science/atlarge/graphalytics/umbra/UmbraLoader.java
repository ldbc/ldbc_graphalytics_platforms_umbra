package science.atlarge.graphalytics.umbra;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;


/**
 * Base class for graph loading in the platform driver.
 */
public class UmbraLoader {

	private static final Logger LOG = LogManager.getLogger();

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

	public void load() throws Exception {
		Connection conn = UmbraUtil.getConnection();

		Statement statement = conn.createStatement();
		statement.executeUpdate("DROP TABLE IF EXISTS u");
		statement.executeUpdate("DROP TABLE IF EXISTS v");
		statement.executeUpdate("DROP TABLE IF EXISTS e");

		String weightAttributeWithoutType = "";
		String weightAttributeWithType = "";
		if (formattedGraph.getGraph().getSourceGraph().hasEdgeProperties()) {
			// the graph is weighted
			weightAttributeWithoutType = ", weight";
			weightAttributeWithType = ", weight FLOAT";
		}

		statement.executeUpdate(String.format("CREATE TABLE v (id INTEGER);"));
		statement.executeUpdate(String.format("CREATE TABLE e (source INTEGER, target INTEGER%s);", weightAttributeWithType));

		String loaderConfiguration = "(DELIMITER ' ', FORMAT csv)";
		String dataDirectoryPath = "/input-data/";
		statement.executeUpdate(String.format(
				"COPY v (id) FROM '%s/%s.v' %s",
				dataDirectoryPath,
				formattedGraph.getGraph().getName(),
				loaderConfiguration
		));
		statement.executeUpdate(String.format(
				"COPY e (source, target%s) FROM '%s/%s.e' (DELIMITER ' ', FORMAT csv)",
				weightAttributeWithoutType,
				dataDirectoryPath,
				formattedGraph.getGraph().getName(),
				loaderConfiguration
		));

		// create undirected table 'u'
		if (formattedGraph.isDirected()) {
			statement.executeUpdate("CREATE TABLE u (source INTEGER, target INTEGER)");
			statement.executeUpdate("INSERT INTO u SELECT target, source FROM e");
			statement.executeUpdate("INSERT INTO u SELECT source, target FROM e");
		} else {
			statement.executeUpdate("CREATE TABLE u AS SELECT source, target FROM e");
		}
		statement.close();
	}

	public void unload() throws Exception {
//		String unloaderDir = platformConfig.getUnloaderPath();
//		commandLine = new CommandLine(Paths.get(unloaderDir).toFile());
//
//		commandLine.addArgument("--graph-name");
//		commandLine.addArgument(formattedGraph.getName());
//
//		commandLine.addArgument("--output-path");
//		commandLine.addArgument(loadedInputPath);
//
//		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
//		LOG.info(String.format("Execute graph unloader with command-line: [%s]", commandString));
//
//		Executor executor = new DefaultExecutor();
//		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
//		executor.setExitValue(0);
//
//		return executor.execute(commandLine);
	}

}
