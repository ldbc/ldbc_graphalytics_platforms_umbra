package science.atlarge.graphalytics.umbra.algorithms.wcc;

import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;
import science.atlarge.graphalytics.umbra.UmbraJob;

/**
 * Breadth First Search job implementation for GraphBLAS. This class is responsible for formatting BFS-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 *
 * @author Bálint Hegyi
 */
public final class WeaklyConnectedComponents extends UmbraJob {

	/**
	 * Creates a new BreadthFirstSearchJob object with all mandatory parameters specified.
	 *  @param platformConfig the platform configuration.
	 * @param inputPath the path to the input graph.
	 */
	public WeaklyConnectedComponents(RunSpecification runSpecification, UmbraConfiguration platformConfig,
                                     String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}

	@Override
	protected void appendAlgorithmParameters() {
		commandLine.addArgument("--algorithm");
		commandLine.addArgument("wcc");
	}
}
