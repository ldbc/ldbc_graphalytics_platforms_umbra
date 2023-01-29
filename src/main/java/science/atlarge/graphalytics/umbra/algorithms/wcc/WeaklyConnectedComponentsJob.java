package science.atlarge.graphalytics.umbra.algorithms.wcc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import science.atlarge.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;
import science.atlarge.graphalytics.umbra.UmbraJob;
import science.atlarge.graphalytics.umbra.UmbraUtil;
import science.atlarge.graphalytics.umbra.algorithms.sssp.SingleSourceShortestPathComputation;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Breadth First Search job implementation for Umbra. This class is responsible for formatting BFS-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class WeaklyConnectedComponentsJob extends UmbraJob {

	/**
	 * Creates a new BreadthFirstSearchJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the path to the input graph.
	 */
	public WeaklyConnectedComponentsJob(RunSpecification runSpecification, UmbraConfiguration platformConfig,
										String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}


	@Override
	public void execute() throws SQLException, ClassNotFoundException, IOException {
		Connection conn = UmbraUtil.getConnection();
		Statement statement = conn.createStatement();

		WeaklyConnectedComponentsComputation weaklyConnectedComponentsComputation = new WeaklyConnectedComponentsComputation(statement);
		weaklyConnectedComponentsComputation.execute();

		// move results to the place expected by the Graphalytics framework
		FileUtils.moveFile(
				new File("scratch/output-data/output.csv"),
				new File(getOutputPath())
		);
	}

}
