package domains.nothing.nothingbackend;

import java.util.Properties;

public enum Settings {
	SQL_HOST, SQL_USER, SQL_PASSWORD,
	SQL_DB, SQL_PORT, MAIL_KEY,
	CLAM_HOST, CLAM_PORT, REGISTRATIONS_BLOCKED,
	STRIPE_TOKEN, MAILINABOX_PASSWORD, MAILINABOX_USER,
	CLOUDFLARE_EMAIL, CLOUDFLARE_KEY;

	private String val;

	Settings() {
	}

	public static void init(Properties properties) {
		for (Settings s : values()) {
			s.set(properties.getProperty(s.name().toLowerCase()));
		}
	}

	public String get() {
		return val;
	}

	public void set(String val) {
		this.val = val;
	}
}
