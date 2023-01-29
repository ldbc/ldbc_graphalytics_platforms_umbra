package science.atlarge.graphalytics.umbra;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Base class for graph loading in the platform driver.
 */
public class UmbraLoader {

	private static final Logger LOG = LogManager.getLogger();

	protected FormattedGraph formattedGraph;
	protected UmbraConfiguration platformConfig;
	protected UmbraLoadComputation umbraLoadComputation;

	/**
	 * Graph loader for Umbra.
	 * @param formattedGraph
	 * @param platformConfig
	 */
	public UmbraLoader(FormattedGraph formattedGraph, UmbraConfiguration platformConfig) throws SQLException, ClassNotFoundException {
		this.formattedGraph = formattedGraph;
		this.platformConfig = platformConfig;
		Connection conn = UmbraUtil.getConnection();
		Statement statement = conn.createStatement();

		umbraLoadComputation = new UmbraLoadComputation(statement,
				formattedGraph.getGraph().getName(),
				formattedGraph.isDirected(),
				formattedGraph.getGraph().getSourceGraph().hasEdgeProperties()
		);
	}

	public void load() throws Exception {
		umbraLoadComputation.load();
	}

	public void unload() throws Exception {
		umbraLoadComputation.unload();
	}

}
