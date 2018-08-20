package domains.nothing.nothingbackend.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.RedisCacher;
import domains.nothing.nothingbackend.objects.APIKey;
import domains.nothing.nothingbackend.objects.User;
import domains.nothing.nothingbackend.util.Antivirus;
import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import domains.nothing.nothingbackend.util.Intervals;
import domains.nothing.nothingbackend.util.Utils;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;

@GetFuckingRatelimitedBitch
@RequestMapping("/api/upload")
@RestController
public class Upload {
	private static final Logger LOGGER = LoggerFactory.getLogger(Upload.class);

	static {
		File uploadDir = new File("data/uploads/");
		if (!uploadDir.isDirectory() && !uploadDir.mkdirs())
			throw new IllegalStateException("Cannot make upload directory");
	}

	/**
	 * Request:
	 * RTFD: http://pomf.readthedocs.io/en/latest/api.html#example-request
	 * <p>
	 * Response:
	 * {
	 * "success": true,
	 * "files":
	 * [
	 * {
	 * "hash": "f0ff06fc9770b96ff040a6016d14140ad6b79570",
	 * "name": "48_circles.gif",
	 * "url": "gltraw.gif",
	 * "size": 4824
	 * }
	 * ]
	 * }
	 * <p>
	 * The user takes URL and prepends the domain and protocol they want.
	 */
	@GetFuckingRatelimitedBitch
	@PostMapping("/pomf")
	public ResponseEntity<JsonNode> pomf(HttpServletRequest r,
										 @RequestHeader("Authorization") String authkey) {
		MultipartHttpServletRequest files;
		try {
			files = new DefaultMultipartHttpServletRequest(r);
		} catch (Exception ignored) {
			return pomfError(HttpStatus.BAD_REQUEST, "Multipart required");
		}

		ArrayNode array = Main.MAPPER.createArrayNode();

		try {
			if (!DatabaseConnection.useKey(authkey)) return pomfError(HttpStatus.UNAUTHORIZED, "Invalid API key");
			User user = DatabaseConnection.getUserByApiKey(authkey);
			if (user == null) return pomfError(HttpStatus.UNAUTHORIZED, "Invalid API key");
			Collection<Part> parts = files.getParts();
			if (parts.size() > 3) return pomfError(HttpStatus.BAD_REQUEST, "Too many files uploading!");
			boolean premium = DatabaseConnection.isPremium(user.getUuid().toString());
			for (Part file : parts) {
				if (!Objects.equals(file.getName(), "files[]")) continue;
				if (!Utils.notEmpty(file.getSubmittedFileName())) continue;
				Pair<String, HttpStatus> error = checkLimits(user.getUuid().toString(), file, premium);
				if (error != null) return pomfError(error.getSecond(), error.getFirst());

				UploadedFile uFile = upload(file, user.getUuid().toString(), authkey);

				array.add(Main.MAPPER.createObjectNode()
					.put("hash", uFile.hash)
					.put("name", file.getSubmittedFileName())
					.put("url", uFile.url
						+ Utils.removeBeforeLast(file.getSubmittedFileName(), '.'))
					.put("size", uFile.size));
			}
		} catch (ServletException ignored) {
			return pomfError(HttpStatus.BAD_REQUEST, "Multipart required");
		} catch (Exception e) {
			LOGGER.error("POMF upload file error", e);
			return pomfError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
		}

		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("success", true)
			.set("files", array),
			HttpStatus.OK);
	}

	@GetFuckingRatelimitedBitch
	@PostMapping("/session")
	public ResponseEntity<String> simple(HttpServletRequest r, HttpServletResponse res)
		throws SQLException, IOException, ServletException, NoSuchAlgorithmException {
		if (!Utils.isLoggedIn(r)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		List<APIKey> apikeys = DatabaseConnection.getAPIKeys((String) r.getSession().getAttribute("user"));
		MultipartHttpServletRequest files;
		try {
			files = new DefaultMultipartHttpServletRequest(r);
		} catch (Exception ignored) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Part part = files.getPart("file");
		String user = (String) r.getSession().getAttribute("user");
		boolean premium = DatabaseConnection.isPremium(user);
		if (part != null) {
			String key = apikeys.size() != 0 ?
				apikeys.get(0).getValue() :
				DatabaseConnection.addKey(user);
			if (!DatabaseConnection.useKey(key))
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

			Pair<String, HttpStatus> error = checkLimits(user, part, premium);
			if (error != null) return new ResponseEntity<>(error.getFirst(), error.getSecond());

			String url = upload(part, user, key).url;
			res.sendRedirect("https://nothing.domains/" + url);
			return new ResponseEntity<>(url, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	private Pair<String, HttpStatus> checkLimits(String user, Part part, boolean premium) throws SQLException {
		if (!premium
			&& DatabaseConnection.uploadsWithinLast(user, 1, Intervals.DAY) > 50) {
			return Pair.of("You've exceeded the free 24h quota (50). " +
				"Please delete some images or wait some time.", HttpStatus.TOO_MANY_REQUESTS);
		}

		if (part.getSize() >= 20_971520) {
			if (!premium || part.getSize() >= 83_886080) {
				return Pair.of("You can only upload up to 20MiB as a non-premium user, and 80MiB as a premium user.", HttpStatus.TOO_MANY_REQUESTS);
			}
		}
		return null;
	}

	private UploadedFile upload(Part file, String userId, String apiKey) throws IOException, NoSuchAlgorithmException, SQLException {
		String hash;
		String url;
		String cType = file.getContentType();
		File tempFile = File.createTempFile(Utils.randomString(16), ".tmp"); // if it collides ill literary kill myself
		FileOutputStream fos = new FileOutputStream(tempFile);

		long l = 0;
		int len;
		byte[] buffer = new byte[0x1000];
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		try (InputStream io = file.getInputStream()) {
			while ((len = io.read(buffer)) > -1) {
				digest.update(buffer, 0, len);
				fos.write(buffer, 0, len);
				l += len;
			}
		}
		fos.flush();
		fos.close();
		boolean download = false;
		if (!Utils.notEmpty(cType)) cType = Files.probeContentType(tempFile.toPath());
		if (Utils.notEmpty(cType)
			&& !cType.startsWith("media/")
			&& !cType.startsWith("image/")
			&& !cType.startsWith("audio/")
			&& !cType.startsWith("video/")
			&& !cType.equals("application/json")
			&& !cType.equals("text/plain")) {
			if (file.getSubmittedFileName() != null)
				download = true;
		}


		hash = new BigInteger(1, digest.digest()).toString(16);
		File f = new File("data/uploads/" + hash);
		if (!f.exists()) {
			List<String> results = Antivirus.scan(tempFile);
			if (!results.isEmpty()) {
				Map<String, Object> dataModel = new HashMap<>();
				dataModel.put("wiruses", results);
				try {
					Main.FREEMARKER_CFG.getTemplate("virus.ftl").process(dataModel, new FileWriter(f));
				} catch (TemplateException e) {
					Files.write(f.toPath(), results);
				}
				cType = "text/html";
			} else Files.move(tempFile.toPath(), f.toPath(), StandardCopyOption.ATOMIC_MOVE);
		}
		if (tempFile.exists()) //noinspection ResultOfMethodCallIgnored
			tempFile.delete();
		String result = DatabaseConnection.addUpload(hash, userId, cType, download, file.getSubmittedFileName());
		try (Jedis j = Main.JEDIS.getResource()) {
			RedisCacher.expire(j, result);
		}
		return new UploadedFile(hash, l, result);
	}

	private ResponseEntity<JsonNode> pomfError(HttpStatus status, String error) {
		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("success", false)
			.put("description", error)
			.put("errorcode", status.value()),
			status);
	}

	@GetFuckingRatelimitedBitch
	@GetMapping("/keys/new")
	public String createKey(HttpServletResponse res, HttpServletRequest req) throws SQLException, IOException {
		if (!Utils.isLoggedIn(req)) {
			res.setStatus(HttpStatus.UNAUTHORIZED.value());
			return "Not logged in!";
		}

		if (DatabaseConnection.getAPIKeys((String) req.getSession().getAttribute("user")).size() >= 5) {
			return "Too many API Keys.";
		}

		return DatabaseConnection.addKey((String) req.getSession().getAttribute("user"));
	}

	@GetFuckingRatelimitedBitch
	@GetMapping("/keys/delete/{key}")
	public String deleteKey(@PathVariable String key, HttpServletResponse res, HttpServletRequest req) throws SQLException {
		if (!Utils.isLoggedIn(req)) {
			res.setStatus(HttpStatus.UNAUTHORIZED.value());
			return "Not logged in!";
		}
		return DatabaseConnection.removeKey(key, (String) req.getSession().getAttribute("user")) ? "Success!" :
			"No such key under your account";
	}

	@GetFuckingRatelimitedBitch
	@GetMapping("/delete/{key}")
	public String delete(@PathVariable String key, HttpServletResponse res, HttpServletRequest req) throws SQLException, IOException {
		if (!Utils.isLoggedIn(req)) {
			res.setStatus(HttpStatus.UNAUTHORIZED.value());
			return "Not logged in!";
		}
		boolean has = DatabaseConnection.removeImage(key, (String) req.getSession().getAttribute("user"));
		if (has) {
			try (Jedis j = Main.JEDIS.getResource()) {
				RedisCacher.expire(j, key);
			}
		}
		return has ? "Success!" : "No such image under your account";
	}

	private static class UploadedFile {
		String hash;
		long size;
		public String url;

		public UploadedFile(String hash, long size, String url) {
			this.hash = hash;
			this.size = size;
			this.url = url;
		}
	}
}