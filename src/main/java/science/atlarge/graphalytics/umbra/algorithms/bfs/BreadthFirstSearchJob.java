package science.atlarge.graphalytics.umbra.algorithms.bfs;

import org.apache.commons.io.FileUtils;
import science.atlarge.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;
import science.atlarge.graphalytics.umbra.UmbraJob;
import science.atlarge.graphalytics.umbra.UmbraUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Breadth First Search job implementation for Umbra. This class is responsible for formatting BFS-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class BreadthFirstSearchJob extends UmbraJob {

	/**
	 * Creates a new BreadthFirstSearchJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the path to the input graph.
	 */
	public BreadthFirstSearchJob(RunSpecification runSpecification, UmbraConfiguration platformConfig,
                                 String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}


	@Override
	public void execute() throws SQLException, ClassNotFoundException, IOException {
		BreadthFirstSearchParameters params = (BreadthFirstSearchParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();
		long sourceVertex = params.getSourceVertex();

		Connection conn = UmbraUtil.getConnection();
		Statement statement = conn.createStatement();

		BreadthFirstSearchComputation breadthFirstSearchComputation = new BreadthFirstSearchComputation(statement, sourceVertex);
		breadthFirstSearchComputation.execute();

		// move results to the place expected by the Graphalytics framework
		FileUtils.moveFile(
				new File("scratch/output-data/output.csv"),
				new File(getOutputPath())
		);
	}

}
