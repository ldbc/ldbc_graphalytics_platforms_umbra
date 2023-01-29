package science.atlarge.graphalytics.umbra.algorithms.pr;

import org.apache.commons.io.FileUtils;
import science.atlarge.graphalytics.domain.algorithms.PageRankParameters;
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
 * PageRank job implementation for Umbra. This class is responsible for formatting PR-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class PageRankJob extends UmbraJob {

    /**
     * Creates a new PageRankJob object with all mandatory parameters specified.
     *
     * @param platformConfig the platform configuration.
     * @param inputPath      the path to the input graph.
     */
    public PageRankJob(RunSpecification runSpecification, UmbraConfiguration platformConfig,
                       String inputPath, String outputPath, Graph benchmarkGraph) {
        super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
    }

    @Override
    public void execute() throws SQLException, IOException, ClassNotFoundException {
        PageRankParameters params = (PageRankParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();

        Connection conn = UmbraUtil.getConnection();
        Statement statement = conn.createStatement();

        PageRankComputation pageRankComputation = new PageRankComputation(statement, params.getNumberOfIterations(), params.getDampingFactor());
        pageRankComputation.execute();

        // move results to the place expected by the Graphalytics framework
        FileUtils.moveFile(
                new File("scratch/output-data/output.csv"),
                new File(getOutputPath())
        );
    }

}
