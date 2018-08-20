package domains.nothing.nothingbackend;

import domains.nothing.nothingbackend.objects.APIKey;
import domains.nothing.nothingbackend.objects.User;
import domains.nothing.nothingbackend.util.CloudflareUtils;
import domains.nothing.nothingbackend.util.UserMailUtils;
import domains.nothing.nothingbackend.util.Utils;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class Templates {

	private static ExpiringMap<String, Object> CACHE = ExpiringMap.builder()
		.expiration(60, TimeUnit.MINUTES)
		.build();

	private Set<String> zoneSet = (Set<String>) CACHE.computeIfAbsent("zones", (k) -> CloudflareUtils.getZones().stream()
		.map(z -> (CloudflareUtils.isZoneWildcard(z) ? "*." : "") + z.name)
		.collect(Collectors.toSet()));

	@GetMapping("/")
	public String welcome(Map<String, Object> model, HttpServletRequest req) throws SQLException {
		Object ayy = Utils.getUserName(req);
		model.put("user", ayy);
		model.put("name", ayy);
		model.put("domains", zoneSet);
		return "index";
	}

	@GetMapping("/tutorial")
	public String tutorial(Map<String, Object> model, HttpServletRequest req) throws SQLException {
		Object ayy = Utils.getUserName(req);
		model.put("user", ayy);
		model.put("name", ayy);
		model.put("domains", zoneSet);
		return "tutorial";
	}


	@GetMapping("/premium")
	public String premium(Map<String, Object> model, HttpServletRequest req) throws SQLException, IOException {
		if (!Utils.isLoggedIn(req)) return "redirect:/";
		User user = DatabaseConnection.getUser(req.getSession().getAttribute("user").toString());
		boolean premium = DatabaseConnection.isPremium(user.getUuid().toString());
		Object ayy = Utils.getUserName(req);
		if (premium) {
			boolean userEmail = UserMailUtils.userEmailExists(user.getName());
			model.put("emailExists", userEmail);
			model.put("domains", CloudflareUtils.getZones().stream().map((z) -> z.name).collect(Collectors.toSet()));
			model.put("recordCount", DatabaseConnection.getRecordTally(user.getUuid().toString()));
			model.put("user", ayy);
			model.put("name", ayy);
			model.put("uuid", user.getUuid().toString());
			model.put("premium", premium);
		} else {
			model.put("emailExists", false);
			model.put("domains", CloudflareUtils.getZones().stream().map((z) -> z.name).collect(Collectors.toSet()));
			model.put("recordCount", "");
			model.put("user", ayy);
			model.put("name", ayy);
			model.put("uuid", user.getUuid().toString());
			model.put("premium", premium);
		}
		return "premium";
	}

	@GetMapping("/shorten")
	public String shorten(Map<String, Object> model, HttpServletRequest req) throws SQLException {
		if (!Utils.isLoggedIn(req)) return "redirect:/";
		Object ayy = Utils.getUserName(req);
		model.put("user", ayy);
		model.put("name", ayy);
		List<APIKey> apikeys = DatabaseConnection.getAPIKeys((String) req.getSession().getAttribute("user"));
		model.put("apikey", (apikeys.size() != 0 ?
			apikeys.get(0).getValue() :
			DatabaseConnection.addKey((String) req.getSession().getAttribute("user"))));
		return "shorten";
	}

	@GetMapping("/reset/{token}")
	public String reset(Map<String, Object> model, @PathVariable String token, HttpServletRequest req) throws SQLException {
		if (Utils.isLoggedIn(req) || !DatabaseConnection.isResetToken(token)) return "redirect:/";
		model.put("token", token);
		return "reset";
	}

	@GetMapping("/forgot")
	public String forgot(Map<String, Object> model, HttpServletRequest req) {
		if (Utils.isLoggedIn(req)) return "redirect:/";
		return "reset-email";
	}

	@GetMapping("/client-area")
	public String clientArea(Map<String, Object> model, HttpServletRequest req) throws SQLException {
		if (!Utils.isLoggedIn(req)) return "redirect:/";
		Object ayy = Utils.getUserName(req);
		model.put("user", ayy);
		model.put("name", ayy);
		List<DatabaseConnection.UploadedImage> imageObjects =
			DatabaseConnection.getImages((String) req.getSession().getAttribute("user"));
		List<Map<String, Object>> images = new ArrayList<>();
		for (DatabaseConnection.UploadedImage m : imageObjects) {
			if (m.deleted != null) continue;
			Map<String, Object> image = new HashMap<>();
			image.put("hash", m.hash);
			image.put("date", m.ts.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME));
			image.put("size", m.getSize());
			image.put("url", m.url);
			images.add(image);
		}
		model.put("images", images);

		List<APIKey> keyObjects = DatabaseConnection.getAPIKeys((String) req.getSession().getAttribute("user"));
		List<Map<String, Object>> keys = new ArrayList<>();
		for (APIKey m : keyObjects) {
			Map<String, Object> key = new HashMap<>();
			key.put("usages", m.getUsages());
			key.put("value", m.getValue());
			keys.add(key);
		}
		model.put("keys", keys);
		return "client-area";
	}

	@GetMapping("/upload")
	public String upload(Map<String, Object> model, HttpServletRequest req) throws SQLException {
		if (!Utils.isLoggedIn(req)) return "redirect:/";
		Object ayy = Utils.getUserName(req);
		model.put("user", ayy);
		model.put("name", ayy);
		return "upload";
	}
}
