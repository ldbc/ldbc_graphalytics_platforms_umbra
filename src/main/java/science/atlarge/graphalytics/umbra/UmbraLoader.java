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
		String loaderDir = platformConfig.getLoaderPath();
		commandLine = new CommandLine(Paths.get(loaderDir).toFile());

		String inputDirectory = FilenameUtils.getFullPath(formattedGraph.getVertexFilePath());

		commandLine.addArgument("--graph-name");
		//commandLine.addArgument(formattedGraph.getName());
		commandLine.addArgument(formattedGraph.getGraph().getName());

		commandLine.addArgument("--input-directory");
		commandLine.addArgument(inputDirectory);

		commandLine.addArgument("--input-vertex-path");
		commandLine.addArgument(formattedGraph.getVertexFilePath());

		commandLine.addArgument("--input-edge-path");
		commandLine.addArgument(formattedGraph.getEdgeFilePath());

		commandLine.addArgument("--output-path");
		commandLine.addArgument(loadedInputPath);

		commandLine.addArgument("--directed");
		commandLine.addArgument(formattedGraph.isDirected() ? "true" : "false");

		commandLine.addArgument("--weighted");
		commandLine.addArgument(formattedGraph.hasEdgeProperties() ? "true" : "false");

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Execute graph loader with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);

		return executor.execute(commandLine);
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
