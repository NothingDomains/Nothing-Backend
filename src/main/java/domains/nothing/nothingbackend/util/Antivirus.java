package domains.nothing.nothingbackend.util;

import domains.nothing.nothingbackend.Settings;
import xyz.capybara.clamav.ClamavClient;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Antivirus {
	private static final ClamavClient CLIENT = new ClamavClient(Settings.CLAM_HOST.get(),
		Integer.parseInt(Settings.CLAM_PORT.get()));

	public static List<String> scan(File file) {
		Map<String, Collection<String>> virusesMap = CLIENT.scan(file.toPath()).getFoundViruses();
		return virusesMap.entrySet().stream().flatMap(a -> a.getValue().stream())
			.distinct().sorted().collect(Collectors.toList());
	}
}
