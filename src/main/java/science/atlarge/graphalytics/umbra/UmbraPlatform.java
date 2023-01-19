package science.atlarge.graphalytics.umbra;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.LoadedGraph;
import science.atlarge.graphalytics.execution.*;
import science.atlarge.graphalytics.umbra.algorithms.bfs.BreadthFirstSearchJob;
import science.atlarge.graphalytics.umbra.algorithms.cdlp.CommunityDetectionLPJob;
import science.atlarge.graphalytics.umbra.algorithms.lcc.LocalClusteringCoefficientJob;
import science.atlarge.graphalytics.umbra.algorithms.pr.PageRankJob;
import science.atlarge.graphalytics.umbra.algorithms.sssp.SingleSourceShortestPathJob;
import science.atlarge.graphalytics.umbra.algorithms.wcc.WeaklyConnectedComponents;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Umbra platform driver for the Graphalytics benchmark.
 */
public class UmbraPlatform implements Platform {

	protected static final Logger LOG = LogManager.getLogger();

	public static final String PLATFORM_NAME = "umbra";
	public UmbraLoader loader;

	@Override
	public void verifySetup() throws Exception {

	}

	@Override
	public LoadedGraph loadGraph(FormattedGraph formattedGraph) throws Exception {
		UmbraConfiguration platformConfig = UmbraConfiguration.parsePropertiesFile();
		loader = new UmbraLoader(formattedGraph, platformConfig);

		LOG.info("Loading graph " + formattedGraph.getName());
		Path loadedPath = Paths.get("./intermediate").resolve(formattedGraph.getName());

		try {
			int exitCode = loader.load(loadedPath.toString());
			if (exitCode != 0) {
				throw new PlatformExecutionException("Umbra exited with an error code: " + exitCode);
			}
		} catch (Exception e) {
			throw new PlatformExecutionException("Failed to load a Umbra dataset.", e);
		}
		LOG.info("Loaded graph " + formattedGraph.getName());
		return new LoadedGraph(formattedGraph, loadedPath.toString());
	}

	@Override
	public void deleteGraph(LoadedGraph loadedGraph) throws Exception {
		LOG.info("Unloading graph " + loadedGraph.getFormattedGraph().getName());
		try {

			int exitCode = loader.unload(loadedGraph.getLoadedPath());
			if (exitCode != 0) {
				throw new PlatformExecutionException("Umbra exited with an error code: " + exitCode);
			}
		} catch (Exception e) {
			throw new PlatformExecutionException("Failed to unload a Umbra dataset.", e);
		}
		LOG.info("Unloaded graph " +  loadedGraph.getFormattedGraph().getName());
	}

	@Override
	public void prepare(RunSpecification runSpecification) throws Exception {

	}

	@Override
	public void startup(RunSpecification runSpecification) throws Exception {
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		Path logDir = benchmarkRunSetup.getLogDir().resolve("platform").resolve("runner.logs");
		UmbraCollector.startPlatformLogging(logDir);
	}

	@Override
	public void run(RunSpecification runSpecification) throws PlatformExecutionException {
		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		RuntimeSetup runtimeSetup = runSpecification.getRuntimeSetup();

		Algorithm algorithm = benchmarkRun.getAlgorithm();
		UmbraConfiguration platformConfig = UmbraConfiguration.parsePropertiesFile();
		String inputPath = runtimeSetup.getLoadedGraph().getLoadedPath();
		String outputPath = benchmarkRunSetup.getOutputDir().resolve(benchmarkRun.getName()).toAbsolutePath().toString();
		Graph benchmarkGraph = benchmarkRun.getGraph();

		UmbraJob job;
		switch (algorithm) {
			case BFS:
				job = new BreadthFirstSearchJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case CDLP:
				job = new CommunityDetectionLPJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case LCC:
				job = new LocalClusteringCoefficientJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case PR:
				job = new PageRankJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case SSSP:
				job = new SingleSourceShortestPathJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case WCC:
				job = new WeaklyConnectedComponents(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			default:
				throw new PlatformExecutionException("Failed to load algorithm implementation.");
		}

		LOG.info("Executing benchmark with algorithm \"{}\" on graph \"{}\".",
				benchmarkRun.getAlgorithm().getName(),
				benchmarkRun.getFormattedGraph().getName());

		try {

			int exitCode = job.execute();
			if (exitCode != 0) {
				throw new PlatformExecutionException("Umbra exited with an error code: " + exitCode);
			}
		} catch (Exception e) {
			throw new PlatformExecutionException("Failed to execute a Umbra job.", e);
		}

		LOG.info("Executed benchmark with algorithm \"{}\" on graph \"{}\".",
				benchmarkRun.getAlgorithm().getName(),
				benchmarkRun.getFormattedGraph().getName());

	}

	@Override
	public BenchmarkMetrics finalize(RunSpecification runSpecification) throws Exception {
		UmbraCollector.stopPlatformLogging();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		Path logDir = benchmarkRunSetup.getLogDir().resolve("platform");

		BenchmarkMetrics metrics = new BenchmarkMetrics();
		metrics.setProcessingTime(UmbraCollector.collectProcessingTime(logDir));
		return metrics;
	}

	@Override
	public void terminate(RunSpecification runSpecification) throws Exception {
		BenchmarkRunner.terminatePlatform(runSpecification);
	}

	@Override
	public String getPlatformName() {
		return PLATFORM_NAME;
	}
}
