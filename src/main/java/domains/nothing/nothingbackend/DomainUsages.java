package domains.nothing.nothingbackend;

import domains.nothing.nothingbackend.scheduler.Task;
import domains.nothing.nothingbackend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DomainUsages {
	private static Map<String, Long> USAGE_MAP = new ConcurrentHashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainUsages.class);

	static {
		try {
			DatabaseConnection.loadUsages(USAGE_MAP::put);
		} catch (SQLException e) {
			LOGGER.error("SQL error", e);
			System.exit(1);
		}
		new Task("Record Usages") {
			@Override
			public void run() {
				try {
					DatabaseConnection.saveUsages(USAGE_MAP);
				} catch (SQLException e) {
					LOGGER.error("SQL error", e);
				}
			}
		}.repeat(30000, 30000);
	}

	public static void incrementDomainUsageForXForwardedHostHeader(String host) {
		USAGE_MAP.put(Utils.extractDomainName(host), USAGE_MAP.getOrDefault(host, 0L) + 1);
	}
}
