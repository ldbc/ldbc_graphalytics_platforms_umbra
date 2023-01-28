package science.atlarge.graphalytics.umbra.algorithms.lcc;

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
	public void cleanup(Statement statement) throws SQLException {
		statement.executeUpdate("DROP TABLE IF EXISTS lcc");
		statement.executeUpdate("DROP VIEW IF EXISTS neighbors");
	}

	@Override
	public void execute() throws SQLException, ClassNotFoundException, IOException {
		Connection conn = UmbraUtil.getConnection();
		Statement statement = conn.createStatement();

		cleanup(statement);

		int multiplier = benchmarkGraph.isDirected() ? 1 : 2;

		LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

		statement.executeUpdate("CREATE VIEW neighbors AS (\n" +
				"\t\t\tSELECT e.source AS vertex, e.target AS neighbor\n" +
				"\t\t\tFROM e\n" +
				"\t\t\tUNION\n" +
				"\t\t\tSELECT e.target AS vertex, e.source AS neighbor\n" +
				"\t\t\tFROM e\n" +
				"\t\t\t)");
		statement.executeUpdate("CREATE TABLE lcc AS\n" +
				"SELECT\n" +
				"id,\n" +
				"CASE WHEN tri = 0 THEN 0.0 ELSE (" + multiplier + " * CAST(tri AS float) / (deg*(deg-1))) END AS value\n" +
				"FROM (\n" +
				"    SELECT\n" +
				"        v.id AS id,\n" +
				"        (SELECT count(*) FROM neighbors WHERE neighbors.vertex = v.id) AS deg,\n" +
				"        (\n" +
				"            SELECT count(*)\n" +
				"            FROM neighbors n1\n" +
				"            JOIN neighbors n2\n" +
				"              ON n1.vertex = n2.vertex\n" +
				"            JOIN e e3\n" +
				"              ON e3.source = n1.neighbor\n" +
				"             AND e3.target = n2.neighbor\n" +
				"            WHERE n1.vertex = v.id\n" +
				"        ) AS tri\n" +
				"    FROM v\n" +
				"    ORDER BY v.id ASC\n" +
				") s\n");

		LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

		// export results
		statement.executeUpdate("COPY (SELECT * FROM lcc ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')");

		// move results to the place expected by the Graphalytics framework
		FileUtils.moveFile(
				new File("scratch/output-data/output.csv"),
				new File(getOutputPath())
		);

		cleanup(statement);
	}

}
