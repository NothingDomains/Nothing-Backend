package domains.nothing.nothingbackend;

import com.zaxxer.hikari.HikariDataSource;
import domains.nothing.nothingbackend.objects.APIKey;
import domains.nothing.nothingbackend.objects.User;
import domains.nothing.nothingbackend.scheduler.Task;
import domains.nothing.nothingbackend.util.Intervals;
import domains.nothing.nothingbackend.util.MailUtil;
import domains.nothing.nothingbackend.util.Utils;
import org.apache.commons.io.FileUtils;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;

public class DatabaseConnection {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);
	private static HikariDataSource DATA_SOURCE = new HikariDataSource();

	static {
		DATA_SOURCE.setMaximumPoolSize(6);
		DATA_SOURCE.setUsername(Settings.SQL_USER.get());
		DATA_SOURCE.setPassword(Settings.SQL_PASSWORD.get());
		DATA_SOURCE.setMaxLifetime(60_000);
		DATA_SOURCE.setLeakDetectionThreshold(60_000);
		DATA_SOURCE.setDriverClassName("org.mariadb.jdbc.Driver");
		DATA_SOURCE.setJdbcUrl("jdbc:mariadb://" + Settings.SQL_HOST.get() + ":" + Settings.SQL_PORT.get() + "/" + Settings.SQL_DB.get());
		new Task("Database cleanup") {
			@SuppressWarnings("ResultOfMethodCallIgnored")
			@Override
			public void run() {
				try (Connection c = getPooledConnection()) {
					// Remove users that haven't verified their emails, and remove old pass resets
					c.createStatement()
						.executeUpdate("DELETE FROM verifications WHERE ts < DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -1 DAY)");
					c.createStatement()
						.executeUpdate("DELETE FROM passwordreset WHERE ts < DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -1 DAY);");
					c.createStatement()
						.executeUpdate("DELETE FROM users WHERE email_verified = 0 AND ts < DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -1 DAY);");
					// Delete images that are scheduled for deletion
					ResultSet images = c.createStatement()
						.executeQuery("SELECT url, hash,\n" +
							"	hash NOT IN (SELECT hash FROM uploads WHERE deleted IS NULL) AS freed\n" +
							"	FROM uploads WHERE deleted IS NOT NULL\n" +
							"	AND deleted < DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -7 DAY);");
					boolean autoCommit = c.getAutoCommit();
					c.setAutoCommit(false);
					PreparedStatement deleter = c.prepareStatement("DELETE FROM uploads WHERE url = ?");
					while (images.next()) {
						deleter.setString(1, images.getString("url"));
						deleter.addBatch();
						if (images.getBoolean("freed")) {
							String hash = images.getString("hash");
							new File("data/uploads/" + hash).delete();
						}
					}
					deleter.executeBatch();
					c.commit();
					c.setAutoCommit(autoCommit);
					// Remove premium users whos subscription expired
					getPremiumSubscriptions().forEach((uuid, timestamp) -> {
						try {
							if (timestamp.plusDays(getPremiumSubscriptionLength(uuid)).isBefore(LocalDateTime.now())) {
								deletePremiumSubscription(uuid);
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					});
				} catch (Exception e) {
					LOGGER.error("DB Cleanup Failed", e);
				}
			}
		}.repeat(30_000, 600_000);

		try {
			Flyway flyway = new Flyway();
			flyway.setDataSource(DATA_SOURCE);
			if (System.getProperty("baseline", "false").equals("true"))
				flyway.baseline();
			flyway.migrate();
		} catch (Exception e) {
			LOGGER.error("Migrations failed", e);
			System.exit(2);
		}
	}

	private DatabaseConnection() {
	}

	public static void addNewUser(String username, String email, String hash, String uuid) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO users " +
				"(username, email, hash, uuid, email_verified) " +
				"VALUES (?, ?, ?, ?, FALSE)");

			stmt.setString(1, username);
			stmt.setString(2, email);
			stmt.setString(3, hash);
			stmt.setString(4, uuid);

			stmt.executeUpdate();
		}
	}

	private static Connection getPooledConnection() throws SQLException {
		return DATA_SOURCE.getConnection();
	}

	public static User getUser(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM users " +
				"WHERE uuid = ? AND email_verified = 1");
			stmt.setString(1, userId);
			ResultSet results = stmt.executeQuery();
			if (!results.next()) return null;
			return new User(userId, results.getString("username"), results.getString("hash"), results.getString("email"));
		}
	}

	public static User getUserFromEmail(String email) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM users " +
				"WHERE email = ?");
			stmt.setString(1, email);
			ResultSet results = stmt.executeQuery();
			if (!results.next()) return null;
			return new User(results.getString("uuid"), results.getString("username"), results.getString("email"), results.getString("hash"));
		}
	}


	public static void insertVerificationEmail(String internalId, String verificationToken) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO verifications " +
				"(internalId, token) VALUES (?, ?)");
			stmt.setString(1, internalId);
			stmt.setString(2, verificationToken);
			stmt.executeUpdate();
		}
	}


	public static void insertPasswordReset(String internalId, String resetToken) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO passwordreset " +
				"(internalId, token) VALUES (?, ?)");
			stmt.setString(1, internalId);
			stmt.setString(2, resetToken);
			stmt.executeUpdate();
		}
	}

	public static boolean resetPassword(String resetToken, String newPassword) throws SQLException {
		try (Connection c = getPooledConnection()) {
			if (newPassword.length() < 8) throw new IllegalArgumentException("Password must be 8 characters!");
			PreparedStatement stmt = c
				.prepareStatement("SELECT internalId FROM passwordreset WHERE token = ?");
			stmt.setString(1, resetToken);
			ResultSet set;
			if ((set = stmt.executeQuery()).next()) {
				stmt = c
					.prepareStatement("DELETE FROM passwordreset WHERE token = ?");
				stmt.setString(1, resetToken);
				stmt.executeUpdate();
				BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
				stmt = c
					.prepareStatement("UPDATE users SET `hash` = ? WHERE uuid = ?");
				stmt.setString(1, encoder.encode(newPassword));
				stmt.setString(2, set.getString("internalId"));
				return stmt.executeUpdate() != 0;
			} else return false;
		}
	}


	public static boolean verifyEmail(String token) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c
				.prepareStatement("SELECT internalId FROM verifications WHERE token = ?");
			stmt.setString(1, token);
			ResultSet set;
			if ((set = stmt.executeQuery()).next()) {
				stmt = c
					.prepareStatement("DELETE FROM verifications WHERE token = ?");
				stmt.setString(1, token);
				stmt.executeUpdate();
				stmt = c
					.prepareStatement("UPDATE users SET email_verified = TRUE WHERE uuid = ?");
				stmt.setString(1, set.getString("internalId"));
				boolean toReturn = stmt.executeUpdate() != 0;
				return toReturn;
			} else return false;
		}
	}

	public static String addKey(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			if (getUser(userId) == null) return null;
			UUID newKey = UUID.randomUUID();
			while (keyExists(newKey.toString())) newKey = UUID.randomUUID();

			PreparedStatement statement = c
				.prepareStatement("INSERT INTO apikeys (apikey, userid) VALUES (" +
					"   ?," +
					"   ?" +
					")");
			statement.setString(1, newKey.toString());
			statement.setString(2, userId);
			statement.executeUpdate();
			return newKey.toString();
		}
	}

	public static boolean useKey(String authkey) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("UPDATE apikeys SET usages = usages + 1 WHERE apikey = ?");
			statement.setString(1, authkey);
			return statement.executeUpdate() != 0;
		}
	}

	public static boolean keyExists(String authkey) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT apikey FROM apikeys WHERE apikey = ?");
			statement.setString(1, authkey);
			return statement.executeQuery().isBeforeFirst();
		}
	}

	public static boolean userExists(String user, String password) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT hash FROM users WHERE (username = ? OR email = ?) AND email_verified = 1");
			statement.setString(1, user);
			statement.setString(2, user);
			ResultSet set = statement.executeQuery();
			return set.next() && new BCryptPasswordEncoder().matches(password, set.getString(1));
		}
	}


	public static void removeVerifications(String internalId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("DELETE FROM verifications WHERE internalId = ?");
			statement.setString(1, internalId);
			statement.executeUpdate();
		}
	}

	public static boolean isUserConflicting(String name, String email) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT * FROM users WHERE username = ? OR email = ?");
			statement.setString(1, name);
			statement.setString(2, email);
			boolean toReturn = statement.executeQuery().isBeforeFirst();
			return toReturn;
		}
	}

	public static boolean resetKeyExists(String passwordResetToken) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT * FROM passwordreset WHERE token = ?");
			statement.setString(1, passwordResetToken);
			return statement.executeQuery().isBeforeFirst();
		}
	}

	public static void removePasswordResets(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("DELETE FROM passwordreset WHERE internalId = ?");
			statement.setString(1, userId);
			statement.executeUpdate();
		}
	}

	public static User getUserByApiKey(String authkey) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT userid FROM apikeys WHERE apikey = ?");
			statement.setString(1, authkey);
			ResultSet set = statement.executeQuery();
			if (set.next())
				return getUser(set.getString("userid"));
			return null;
		}
	}

	public static List<UploadedImage> getImages(String user) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM uploads " +
				"WHERE uploader = ?");
			stmt.setString(1, user);
			ResultSet res = stmt.executeQuery();
			List<UploadedImage> ret = new ArrayList<>();
			while (res.next())
				ret.add(new UploadedImage(res));
			return ret;
		}
	}

	public static User getUserFromLogin(String user) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM users " +
				"WHERE email = ? OR username = ?");
			stmt.setString(1, user);
			stmt.setString(2, user);
			ResultSet results = stmt.executeQuery();
			if (!results.next()) return null;
			return new User(results.getString("uuid"), results.getString("username"), results.getString("email"), results.getString("hash"));
		}
	}

	public static boolean removeKey(String key, String user) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c
				.prepareStatement("DELETE FROM apikeys WHERE userId = ? AND apikey = ?");
			stmt.setString(1, user);
			stmt.setString(2, key);
			return stmt.executeUpdate() > 0;
		}
	}

	public static boolean accountUnverified(String id) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT email_verified FROM users WHERE username = ? OR email = ?");
			statement.setString(1, id);
			statement.setString(2, id);
			ResultSet set = statement.executeQuery();
			return !set.next() || !set.getBoolean(1);
		}
	}

	public static List<APIKey> getAPIKeys(String user) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c.prepareStatement("SELECT apikey, usages FROM apikeys " +
				"WHERE userid = ?");
			stmt.setString(1, user);
			ResultSet res = stmt.executeQuery();
			List<APIKey> ret = new ArrayList<>();
			while (res.next()) ret.add(new APIKey(res.getInt(2), res.getString(1)));
			return ret;
		}
	}

	public static boolean removeImage(String key, String user) throws SQLException, IOException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("UPDATE uploads SET deleted = CURRENT_TIMESTAMP WHERE url = ? AND uploader = ?");
			statement.setString(1, key);
			statement.setString(2, user);
			return statement.executeUpdate() >= 1;
		}
	}

	public static String getTarget(String shortened) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT url FROM shortened_urls WHERE hash = ?");
			statement.setString(1, shortened);
			ResultSet res = statement.executeQuery();
			if (res.next())
				return res.getString(1);
			return null;
		}
	}

	public static void addUrl(String url, String shortened, String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("INSERT INTO shortened_urls (hash, url, userid) VALUES (" +
					"   ?," +
					"   ?," +
					"   ?" +
					")");
			statement.setString(1, shortened);
			statement.setString(2, url);
			statement.setString(3, userId);
			statement.executeUpdate();
		}
	}

	public static boolean isResetToken(String token) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT token FROM passwordreset WHERE token = ?");
			statement.setString(1, token);
			return statement.executeQuery().isBeforeFirst();
		}
	}

	public static boolean hasCustomerId(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT custId " +
					"FROM users " +
					"WHERE `uuid` = ?");
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			return rs.next() && rs.getString("custId") != null;
		}
	}

	public static String getCustomerId(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT custId " +
					"FROM users " +
					"WHERE `uuid` = ?");
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
				return rs.getString(1);
			return null;
		}
	}

	public static String getUUIDFromCustomerId(String customerId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT uuid " +
				"FROM users " +
				"WHERE `custId` = ?");
			statement.setString(1, customerId);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
				return rs.getString(1);
			return null;
		}

	}

	public static void setCustomerId(String customerId, String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("UPDATE `users` " +
					"SET `custId` = ? " +
					"WHERE `uuid` = ?");
			statement.setString(1, customerId);
			statement.setString(2, userId);
			statement.execute();
		}
	}

	public static boolean isPremium(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT premium " +
					"FROM users " +
					"WHERE `uuid` = ? AND `premium` = TRUE");
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			return rs.next();
		}
	}

	public static void setPremium(boolean premium, String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("UPDATE `users` SET `premium` = ? WHERE `uuid` = ?");
			statement.setBoolean(1, premium);
			statement.setString(2, userId);
			statement.execute();
		}
	}

	public static void renewPremiumSubscription(String userId, int days) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("REPLACE INTO `premium`" +
					" (uuid, ts, lengthInDays) " +
					"VALUES(?, ?, ?)");
			statement.setString(1, userId);
			statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
			statement.setInt(3, days);
			statement.execute();
		}
		setPremium(true, userId);
	}


	public static int getPremiumSubscriptionLength(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c
				.prepareStatement("SELECT `lengthInDays` FROM premium WHERE uuid = ?");
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}


	public static void deletePremiumSubscription(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("DELETE FROM `premium` WHERE `uuid` = ?");
			statement.setString(1, userId);
			statement.execute();
			setPremium(false, userId);
			MailUtil.sendPremiumExpiredEmail(getUser(userId).getEmail(), getUser(userId).getName());
		}
	}

	public static Map<String, LocalDateTime> getPremiumSubscriptions() throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement statement = c
				.prepareStatement("SELECT * FROM `premium`");
			ResultSet rs = statement.executeQuery();
			HashMap<String, LocalDateTime> toReturn = new HashMap<>();
			while (rs.next())
				toReturn.put(rs.getString(1), rs.getTimestamp(2).toLocalDateTime());
			return toReturn;
		}
	}

	public static void addRecordTally(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c
				.prepareStatement("UPDATE `premium` SET `recordsCreated` = recordsCreated+1 WHERE uuid = ?");
			stmt.setString(1, userId);
			stmt.execute();
		}
	}

	public static int getRecordTally(String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c
				.prepareStatement("SELECT `recordsCreated` FROM `premium` WHERE uuid = ?");
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			return 0;
		}
	}

	public static boolean changeUsername(String newUsername, String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c
				.prepareStatement("UPDATE `users` SET `username` = ? WHERE `uuid` = ?");
			stmt.setString(1, newUsername);
			stmt.setString(2, getUser(userId).getUuid().toString());
			return stmt.execute();
		}
	}

	public static boolean changeEmail(String newEmail, String userId) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement stmt = c
				.prepareStatement("UPDATE `users` SET `email` = ? WHERE `uuid` = ?");
			stmt.setString(1, newEmail);
			stmt.setString(2, getUser(userId).getUuid().toString());
			return stmt.execute();
		}
	}

	public static UploadedImage getUpload(String image) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement st = c.prepareStatement("SELECT * FROM uploads WHERE url = ? AND deleted IS NULL");
			st.setString(1, image);
			ResultSet set = st.executeQuery();
			return set.next() ? new UploadedImage(set) : null;
		}
	}

	public static void addUpload(String image, String hash, String user, String contentType, boolean download, String submittedFileName) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement st = c.prepareStatement("INSERT INTO uploads (url, hash, uploader, " +
				"type, download, filename) VALUES (?, ?, ?, ?, ?, ?)");
			st.setString(1, image);
			st.setString(2, hash);
			st.setString(3, user);
			st.setString(4, contentType);
			st.setBoolean(5, download);
			if (submittedFileName == null) submittedFileName = image;
			st.setString(6, submittedFileName);
			st.executeUpdate();
		}
	}

	public static String addUpload(String hash, String userId, String contentType, boolean download, String submittedFileName) throws SQLException {
		String key = Utils.randomString(9);
		while (uploadExists(key)) key = Utils.randomString(9);
		addUpload(key, hash, userId, contentType, download, submittedFileName);
		return key;
	}

	public static boolean uploadExists(String key) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement st = c.prepareStatement("SELECT * FROM uploads WHERE url = ?");
			st.setString(1, key);
			ResultSet set = st.executeQuery();
			return set.isBeforeFirst();
		}
	}

	public static int uploadsWithinLast(String user, int interval, Intervals unit) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement st = c.prepareStatement("SELECT COUNT(url) FROM uploads WHERE uploader = ? " +
				"AND ts > DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -" + interval + " " +
				unit.toString().toUpperCase() + ")");
			st.setString(1, user);
			ResultSet set = st.executeQuery();
			set.next();
			return set.getInt(1);
		}
	}

	public static void loadUsages(BiConsumer<String, Long> put) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement st = c.prepareStatement("SELECT * FROM dusage;");
			ResultSet s = st.executeQuery();
			while (s.next())
				put.accept(s.getString("domain"), s.getLong("uses"));
		}
	}

	public static void saveUsages(Map<String, Long> usageMap) throws SQLException {
		try (Connection c = getPooledConnection()) {
			PreparedStatement ps = c.prepareStatement("INSERT INTO dusage (domain, uses) VALUES (?, ?)" +
				"ON DUPLICATE KEY UPDATE uses = VALUES(uses)");
			boolean ac = c.getAutoCommit();
			c.setAutoCommit(false);
			for (Map.Entry<String, Long> u : usageMap.entrySet()) {
				ps.setString(1, u.getKey());
				ps.setLong(2, u.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
			c.setAutoCommit(ac);
		}
	}

	public static class UploadedImage {
		public String uploader, hash, url, type, filename;
		public boolean download, exists = true;
		public LocalDateTime ts, deleted;

		public UploadedImage() {
		}

		private UploadedImage(ResultSet set) throws SQLException {
			uploader = set.getString("uploader");
			hash = set.getString("hash");
			url = set.getString("url");
			type = set.getString("type");
			ts = set.getTimestamp("ts").toLocalDateTime();
			filename = set.getString("filename");
			download = set.getBoolean("download");
			Timestamp t = set.getTimestamp("deleted");
			if (t != null)
				deleted = t.toLocalDateTime();
		}

		public String getSize() {
			return FileUtils.byteCountToDisplaySize(new File("data/uploads/" + hash).length());
		}
	}
}

