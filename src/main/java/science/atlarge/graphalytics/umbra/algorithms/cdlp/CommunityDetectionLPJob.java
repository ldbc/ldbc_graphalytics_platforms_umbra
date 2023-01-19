package science.atlarge.graphalytics.umbra.algorithms.cdlp;

import science.atlarge.graphalytics.domain.algorithms.CommunityDetectionLPParameters;
import science.atlarge.graphalytics.domain.algorithms.PageRankParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;
import science.atlarge.graphalytics.umbra.UmbraJob;

/**
 * Community Detection by job implementation for Umbra. This class is responsible for formatting CDLP-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class CommunityDetectionLPJob extends UmbraJob {

	/**
	 * Creates a new LocalClusteringCoefficientJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the path to the input graph.
	 */
	public CommunityDetectionLPJob(RunSpecification runSpecification, UmbraConfiguration platformConfig,
                                   String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}

	@Override
	protected void appendAlgorithmParameters() {
		commandLine.addArgument("--algorithm");
		commandLine.addArgument("cdlp");

		CommunityDetectionLPParameters params =
				(CommunityDetectionLPParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();
		commandLine.addArgument("--max-iteration");
		commandLine.addArgument(Integer.toString(params.getMaxIterations()));
	}
}
