package science.atlarge.graphalytics.umbra;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Collection of configurable platform options.
 */
public final class UmbraConfiguration {

	protected static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String NUM_THREADS_KEY = "platform.umbra.num-threads";

	private int numThreads = 1;

	/**
	 * Creates a new UmbraConfiguration object to capture all platform parameters that are not specific to any algorithm.
	 */
	public UmbraConfiguration(){
	}

	/**
	 * @param numThreads the number of threads to use on each machine
	 */
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	/**
	 * @return the number of threads to use on each machine
	 */
	public int getNumThreads() {
		return numThreads;
	}


	public static UmbraConfiguration parsePropertiesFile() {

		UmbraConfiguration platformConfig = new UmbraConfiguration();

		Configuration configuration = null;
		try {
			configuration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
		} catch (Exception e) {
			LOG.warn(String.format("Failed to load configuration from %s", BENCHMARK_PROPERTIES_FILE));
			throw new GraphalyticsExecutionException("Failed to load configuration. Benchmark run aborted.", e);
		}

		Integer numThreads = configuration.getInteger(NUM_THREADS_KEY, null);
		if (numThreads != null) {
			platformConfig.setNumThreads(numThreads);
		} else {
			platformConfig.setNumThreads(1);
		}

		return platformConfig;
	}

}
