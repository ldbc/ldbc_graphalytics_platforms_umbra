package science.atlarge.graphalytics.umbra.algorithms.bfs;

import org.apache.commons.io.FileUtils;
import science.atlarge.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import science.atlarge.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;
import science.atlarge.graphalytics.umbra.UmbraJob;
import science.atlarge.graphalytics.umbra.UmbraUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
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

		statement.executeUpdate("DROP TABLE IF EXISTS frontier");
		statement.executeUpdate("DROP TABLE IF EXISTS next");
		statement.executeUpdate("DROP TABLE IF EXISTS seen");
		statement.executeUpdate("DROP TABLE IF EXISTS bfs");

		LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

		statement.executeUpdate("CREATE TABLE frontier(id INTEGER)");
		statement.executeUpdate("CREATE TABLE next(id INTEGER)");
		statement.executeUpdate("CREATE TABLE seen(id INTEGER, level INTEGER)");

		int level = 0;
		statement.executeUpdate(String.format("INSERT INTO next VALUES (%d)", sourceVertex));
		statement.executeUpdate(String.format("INSERT INTO seen (SELECT id, %d FROM next)", level));
		statement.executeUpdate("DELETE FROM frontier");
		statement.executeUpdate("INSERT INTO frontier (SELECT * FROM next)");
		statement.executeUpdate("DELETE FROM next");

		while (true) {
			level++;
			statement.executeUpdate("INSERT INTO next " +
					"SELECT DISTINCT e.target " +
					"  FROM frontier JOIN e ON e.source = frontier.id " +
					" WHERE NOT EXISTS (SELECT 1 FROM seen WHERE id = e.target)"
			);

			ResultSet resultSet = statement.executeQuery("SELECT count(id) AS count FROM next");
			resultSet.next();
			long count = resultSet.getLong(1);
			if (count == 0) {
				break;
			}

			statement.executeUpdate(String.format("INSERT INTO seen (SELECT id, %d FROM next)", level));
			statement.executeUpdate("DELETE FROM frontier");
			statement.executeUpdate("INSERT INTO frontier (SELECT * FROM next)");
			statement.executeUpdate("DELETE FROM next");
		}

		statement.executeUpdate(
			"CREATE TABLE bfs AS " +
			"  SELECT v.id, coalesce(seen.level, 9223372036854775807) AS level " +
			"  FROM v " +
			"  LEFT JOIN seen ON seen.id = v.id"
		);

		LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

		// export results
		statement.executeUpdate("COPY (SELECT * FROM bfs ORDER BY id ASC) TO '/output-data/output.csv' (DELIMITER ' ')");
	
		// move results to the place expected by the Graphalytics framework
		FileUtils.moveFile(
				new File("scratch/output-data/output.csv"),
				new File(getOutputPath())
		);
	}
}
