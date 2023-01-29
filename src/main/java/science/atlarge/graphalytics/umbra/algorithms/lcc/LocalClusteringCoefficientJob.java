package science.atlarge.graphalytics.umbra.algorithms.lcc;

import org.apache.commons.io.FileUtils;
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
	public void execute() throws SQLException, ClassNotFoundException, IOException {
		Connection conn = UmbraUtil.getConnection();
		Statement statement = conn.createStatement();

		LocalClusteringCoefficientComputation localClusteringCoefficientComputation = new LocalClusteringCoefficientComputation(statement);
		localClusteringCoefficientComputation.execute();

		// move results to the place expected by the Graphalytics framework
		FileUtils.moveFile(
				new File("scratch/output-data/output.csv"),
				new File(getOutputPath())
		);
	}

}
