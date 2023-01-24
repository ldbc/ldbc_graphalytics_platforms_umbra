package science.atlarge.graphalytics.umbra.algorithms.sssp;

import science.atlarge.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;
import science.atlarge.graphalytics.umbra.UmbraJob;

/**
 * Single Source Shortest Path job implementation for Umbra. This class is responsible for formatting SSSP-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class SingleSourceShortestPathJob extends UmbraJob {

	/**
	 * Creates a new SingleSourceShortestPathJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the path to the input graph.
	 */
	public SingleSourceShortestPathJob(RunSpecification runSpecification, UmbraConfiguration platformConfig,
									   String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}

	@Override
	protected void appendAlgorithmParameters() {
		commandLine.addArgument("--algorithm");
		commandLine.addArgument("sssp");

		SingleSourceShortestPathsParameters params =
				(SingleSourceShortestPathsParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();
		commandLine.addArgument("--source-vertex");
		commandLine.addArgument(Long.toString(params.getSourceVertex()));
	}
}
