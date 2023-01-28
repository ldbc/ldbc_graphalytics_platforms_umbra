package science.atlarge.graphalytics.umbra.algorithms.lcc;

import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;
import science.atlarge.graphalytics.umbra.UmbraJob;

/**
 * Local Clustering Coefficient job implementatione for Umbra. This class is responsible for formatting LCC-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class LocalClusteringCoefficientJob extends UmbraJob {

	/**
	 * Creates a new LocalClusteringCoefficientJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath th path to the input graph.
	 */
	public LocalClusteringCoefficientJob(RunSpecification runSpecification, UmbraConfiguration platformConfig,
										 String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}

	@Override
	public void execute() {

	}

}
