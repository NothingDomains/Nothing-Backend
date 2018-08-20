package domains.nothing.nothingbackend.util;

import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.objects.User;
import org.apache.commons.lang.StringUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class Utils {
	private static final char[] RANDOM_LETTER_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	public static void argNotNull(String arg, Object value, String exceptionMessage) {
		if (value == null)
			throw new IllegalArgumentException(String.format("%s: cannot be empty. Additional info: %s", arg, exceptionMessage));
	}

	public static void argNotNull(String arg, Object value) {
		if (value == null)
			throw new IllegalArgumentException(String.format("%s: cannot be empty.", arg));
	}

	public static boolean isValidEmailAddress(String email) {
		if (email == null || email.length() > 40) return false;
		try {
			new InternetAddress(email).validate();
			return true;
		} catch (AddressException ex) {
			return false;
		}
	}

	public static <T> void argValid(String message, T value) {
		argValid(message, value, Objects::nonNull);
	}

	public static <T> void argValid(String message, T value, Predicate<T> check) {
		if (!check.test(value)) throw new IllegalArgumentException(message);
	}

	public static boolean notEmpty(String s) {
		return s != null && !s.isEmpty();
	}

	public static String randomString(int count) {
		StringBuilder s = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			s.append(RANDOM_LETTER_SET[ThreadLocalRandom.current().nextInt(RANDOM_LETTER_SET.length)]);
		}
		return s.toString();
	}

	public static boolean isLoggedIn(HttpServletRequest request) {
		Object userName = request.getSession().getAttribute("user");
		return userName instanceof String && Utils.notEmpty((String) userName);
	}

	public static String getUserName(HttpServletRequest req) throws SQLException {
		if (!isLoggedIn(req)) return "";
		User u = DatabaseConnection.getUser((String) req.getSession().getAttribute("user"));
		if (u == null) return null;
		return u.getName();
	}

	public static String removeAfterLast(String s, char c) {
		if (!s.contains(String.valueOf(c))) return s;
		return s.substring(0, s.lastIndexOf(c));
	}

	public static String removeBeforeLast(String s, char c) {
		if (!s.contains(String.valueOf(c))) return "";
		return s.substring(s.lastIndexOf(c));
	}

	public static String extractDomainName(String host) {
		if (!host.contains(".")) return host;
		if (StringUtils.countMatches(host, ".") == 1) return host;
		String[] arr = host.split("\\.");
		return arr[arr.length - 2] + '.' + arr[arr.length - 1];
	}
}
