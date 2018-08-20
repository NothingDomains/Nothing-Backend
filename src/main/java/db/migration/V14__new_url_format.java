package db.migration;

import org.apache.commons.io.IOUtils;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Performs a migration to the new url format
 */
public class V14__new_url_format extends BaseJdbcMigration {
	private static final Logger LOGGER = LoggerFactory.getLogger(V14__new_url_format.class);

	@Override
	public void migrate(Connection connection) throws Exception {
		long time = System.currentTimeMillis();
		connection.createStatement().execute("CREATE TABLE uploads (\n" +
			"   url 		VARCHAR(9) PRIMARY KEY,\n" +
			"   hash 		VARCHAR(40) NOT NULL,\n" +
			"   uploader 	VARCHAR(36) NOT NULL,\n" +
			"	type		VARCHAR(128) DEFAULT 'text/plain',\n" +
			"	deleted		TIMESTAMP NULL,\n" +
			"	ts 			TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
			");");

		File uploadsDir = new File("data/uploads");
		Set<String> ids = new HashSet<>();
		ResultSet uploads = connection.createStatement().executeQuery("SELECT userId, hash, ts FROM logs");
		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		PreparedStatement statement =
			connection.prepareStatement("INSERT INTO uploads (uploader, hash, ts, url, deleted, type) VALUES (?, ?, ?, ?, ?, ?)");
		while (uploads.next()) {
			String ohash = uploads.getString("hash").substring(0, 7);
			if (new File("data/uploads/" + ohash).exists() && ids.add(ohash)) {
				statement.setString(1, uploads.getString("userId"));

				String hash = uploads.getString("hash");
				Files.move(Paths.get("data/uploads/" + ohash),
					Paths.get("data/uploads/" + hash), StandardCopyOption.ATOMIC_MOVE);
				File typeFile = new File("data/uploads/" + ohash + ".meta");
				try (FileReader metaReader = new FileReader(typeFile)) {
					statement.setString(6, IOUtils.toString(metaReader));
				}
				//noinspection ResultOfMethodCallIgnored
				typeFile.delete();
				File hideFile = new File("data/uploads/" + ohash + ".hide");
				if (hideFile.exists()) {
					statement.setTimestamp(5, new Timestamp(hideFile.lastModified()));
					//noinspection ResultOfMethodCallIgnored
					hideFile.delete();
				} else statement.setNull(5, Types.TIMESTAMP);

				statement.setString(2, hash);
				statement.setTimestamp(3, uploads.getTimestamp("ts"));
				statement.setString(4, ohash);
				statement.addBatch();
			}
		}
		statement.executeBatch();
		if (autoCommit) connection.commit();
		connection.setAutoCommit(autoCommit);

		connection.createStatement().execute("DROP TABLE logs;");
		File[] files = new File("data/uploads/").listFiles((dir, name) -> name.length() == 7
			|| name.endsWith(".meta"));
		if (files != null)
			for (File f : files)
				//noinspection ResultOfMethodCallIgnored
				f.delete();
		long end = System.currentTimeMillis() - time;
		LOGGER.info(String.format("Migration completed in %.2fs (%dms)", end / 1000., end));
	}
}
