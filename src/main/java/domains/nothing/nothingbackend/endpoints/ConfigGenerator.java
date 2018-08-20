package domains.nothing.nothingbackend.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.objects.APIKey;
import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import domains.nothing.nothingbackend.util.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/config/{prefix}")
public class ConfigGenerator {

	@GetMapping(path = "/sharex", produces = "application/json; charset=utf-8")
	@GetFuckingRatelimitedBitch
	public String sharex(@RequestParam(required = false) String download, @PathVariable String prefix,
						 HttpServletRequest req, HttpServletResponse res) throws SQLException, JsonProcessingException {
		if (!Utils.isLoggedIn(req)) {
			res.setStatus(HttpStatus.UNAUTHORIZED.value());
			return "Not logged in!";
		}
		List<APIKey> apikeys = DatabaseConnection.getAPIKeys((String) req.getSession().getAttribute("user"));
		String key = apikeys.size() != 0 ?
			apikeys.get(0).getValue() :
			DatabaseConnection.addKey((String) req.getSession().getAttribute("user"));

		try {
			if (download != null)
				res.setHeader("Content-Disposition", "attachment; filename=\"NothingDomains.sxcu\"");
			return Main.PRETTY_MAPPER.writeValueAsString(new ShareXConfig(key, prefix));
		} catch (URISyntaxException e) {
			return "Bad syntax for prefix";
		}
	}

	@GetMapping(path = "/kshare", produces = "application/json; charset=utf-8")
	@GetFuckingRatelimitedBitch
	public String kshare(@RequestParam(required = false) String download, @PathVariable String prefix,
						 HttpServletRequest req, HttpServletResponse res) throws SQLException, JsonProcessingException {
		if (!Utils.isLoggedIn(req)) {
			res.setStatus(HttpStatus.UNAUTHORIZED.value());
			return "Not logged in!";
		}
		List<APIKey> apikeys = DatabaseConnection.getAPIKeys((String) req.getSession().getAttribute("user"));
		String key = apikeys.size() != 0 ?
			apikeys.get(0).getValue() :
			DatabaseConnection.addKey((String) req.getSession().getAttribute("user"));

		try {
			if (download != null)
				res.setHeader("Content-Disposition", "attachment; filename=\"NothingDomains.uploader\"");
			return Main.PRETTY_MAPPER.writeValueAsString(new KShareConfig(key, prefix));
		} catch (URISyntaxException e) {
			return "Bad syntax for prefix";
		}
	}

	@SuppressWarnings("UnusedAssignment")
	private class ShareXConfig {
		@JsonProperty
		String Name = "Nothing.Domains (";
		@JsonProperty
		String DestinationType = "ImageUploader, TextUploader, FileUploader";
		@JsonProperty
		String RequestURL = "https://nothing.domains/api/upload/pomf";
		@JsonProperty
		String FileFormName = "files[]";
		@JsonProperty
		AuthHeaders Headers;
		@JsonProperty
		String URL;

		public ShareXConfig(String key, String prefix) throws URISyntaxException {
			Name += prefix + ")";
			Headers = new AuthHeaders(key);
			URL = "https://" + prefix;
			new URI(URL);
			URL += "/$json:files[0].url$";
		}
	}

	private class AuthHeaders {
		@JsonProperty
		String Authorization;

		AuthHeaders(String auth) {
			Authorization = auth;
		}
	}

	private class KShareConfig {
		@JsonProperty
		String name = "Nothing Domains (";
		@JsonProperty
		String desc = "Nothing Domains uploader";
		@JsonProperty
		String target = "https://nothing.domains/api/upload/pomf";
		@JsonProperty
		AuthHeaders headers;
		@JsonProperty
		String format = "multipart-form-data";
		@JsonProperty
		KShareBody[] body = {new KShareBody()};
		@JsonProperty("return")
		String ret = ".files.0.url";
		@JsonProperty
		String return_prepend;

		public KShareConfig(String key, String prefix) throws URISyntaxException {
			name += prefix + ")";
			headers = new AuthHeaders(key);
			return_prepend = "https://" + prefix + "/";
			new URI(return_prepend);
		}

		private class KShareBody {
			@JsonProperty("__Content-Type")
			String type = "/%contenttype/";
			@JsonProperty
			String filename = "/image.%format/";
			@JsonProperty
			String name = "files[]";
			@JsonProperty
			String body = "/%imagedata/";
		}
	}
}
