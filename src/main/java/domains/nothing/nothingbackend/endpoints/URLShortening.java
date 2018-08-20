package domains.nothing.nothingbackend.endpoints;

import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.objects.User;
import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

@GetFuckingRatelimitedBitch
@RestController
@RequestMapping("/api/url")
public class URLShortening {

	@GetFuckingRatelimitedBitch
	@GetMapping("/shorten")
	public String postUrl(@RequestParam String url, @RequestHeader("Authorization") String authorization,
	                      HttpServletResponse response) throws SQLException {
		if (url == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "URL Missing";
		}
		if (authorization == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return "Unauthorized";
		}
		User u = DatabaseConnection.getUserByApiKey(authorization);
		if (u == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return "Unauthorized";
		}
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "Not a valid URL.";
		}
		return shortenURL(url, authorization, u.getUuid().toString());
	}

	private String shortenURL(String url, String authorization, String userId) throws SQLException {
		String shortened = "u" + RandomStringUtils.randomAlphanumeric(7);
		while (DatabaseConnection.getTarget(shortened) != null)
			shortened = "u" + RandomStringUtils.randomAlphanumeric(7);
		DatabaseConnection.addUrl(url, shortened, userId);
		return shortened;
	}
}
