package domains.nothing.nothingbackend.endpoints;

import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.RedisCacher;
import domains.nothing.nothingbackend.ResourceNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

@RestController
public class ImageViewer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageViewer.class);

	@GetMapping("/{image}")
	public ResponseEntity<Resource> viewer(@PathVariable String image, @RequestParam(required = false) String download, HttpServletResponse response,
										   HttpServletRequest request) throws SQLException, IOException {
		if (image.contains("."))
			image = image.substring(0, image.lastIndexOf('.') - 1); // fuck extensions
		if (!StringUtils.isAlphanumeric(image)
			|| (image.length() != 9 && // new url format
			image.length() != 7 && // old url format
			(image.length() != 8 && !image.startsWith("u")))) { // shortened links
			throw new ResourceNotFoundException();
		}

		if (image.startsWith("u") && image.length() == 8) {
			String s = DatabaseConnection.getTarget(image);
			if (s == null) throw new ResourceNotFoundException();
			if (request.getParameter("preview") != null)
				return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(s.getBytes())), HttpStatus.OK);

			response.sendRedirect(s);
			return null;
		}

		DatabaseConnection.UploadedImage img;
		try (Jedis j = Main.JEDIS.getResource()) {
			img = RedisCacher.getImage(j, image);
			if (img == null) {
				img = DatabaseConnection.getUpload(image);
				if (img == null) {
					RedisCacher.insertNullImage(j, image);
					throw new ResourceNotFoundException();
				} else RedisCacher.insertImage(j, img);
			} else if (!img.exists)
				throw new ResourceNotFoundException();
		}
		image = img.hash;

		File reqImage = new File("data/uploads/" + image);
		File meta = new File("data/uploads/" + image + ".meta");

		if (reqImage.exists() && img.deleted == null) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType(img.type));
				if (img.download || download != null)
					response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" +
						URLEncoder.encode(img.filename, "UTF-8").replace("+", "%20"));
				return new ResponseEntity<>(new FileSystemResource(reqImage), headers, HttpStatus.OK);
			} catch (Exception e) {
				LOGGER.error("Could not display " + image, e);
				throw new ResourceNotFoundException();
			}
		}
		throw new ResourceNotFoundException();
	}

	@GetMapping("/favicon.ico")
	public Resource favicon() {
		return new ClassPathResource("/favicon.ico");
	}
}
