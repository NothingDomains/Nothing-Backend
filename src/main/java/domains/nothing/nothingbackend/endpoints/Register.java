package domains.nothing.nothingbackend.endpoints;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.Settings;
import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import domains.nothing.nothingbackend.util.MailUtil;
import domains.nothing.nothingbackend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@GetFuckingRatelimitedBitch
@RequestMapping("/api/register")
@RestController
public class Register {
	private static final Logger LOGGER = LoggerFactory.getLogger(Register.class);

	static class User {
		@JsonProperty
		String name;
		@JsonProperty
		String password;
		@JsonProperty
		String email;
		UUID internalId = UUID.randomUUID();
	}

	@GetFuckingRatelimitedBitch
	@PostMapping
	public ResponseEntity<JsonNode> register(@Valid @RequestBody User req) throws IOException {
		if ("true".equals(Settings.REGISTRATIONS_BLOCKED.get()))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "Registrations are currently closed"),
				HttpStatus.valueOf(403));
		try {
			Utils.argValid("Username cannot be empty", req.name, Utils::notEmpty);
			Utils.argValid("Username cannot be more than 40 characters", req.name, n -> n.length() < 40);
			Utils.argValid("Password cannot be empty", req.password, Utils::notEmpty);
			Utils.argValid("Password must be at least 8 characters long!", req.password, n -> n.length() >= 8);
			Utils.argValid("Email is invalid or not provided", req.email, Utils::isValidEmailAddress);
		} catch (Exception e) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", e.getMessage()),
				HttpStatus.valueOf(400));
		}

		try {
			if (DatabaseConnection.isUserConflicting(req.name, req.email))
				return new ResponseEntity<>(Main.MAPPER.createObjectNode()
					.put("success", false)
					.put("error", "Email or name taken"),
					HttpStatus.valueOf(500));
		} catch (SQLException e) {
			LOGGER.error("SQL Error with user conflict checking", e);
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "SQL Error has occurred. This incident been logged."),
				HttpStatus.valueOf(500));
		}

		if (!insertUser(req)) return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("success", false)
			.put("error", "SQL Error has occurred. This incident been logged."),
			HttpStatus.valueOf(500));

		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("success", true), HttpStatus.valueOf(200));
	}

	@GetFuckingRatelimitedBitch
	@GetMapping("/verify/{token}")
	public ResponseEntity<String> verify(@PathVariable("token") String token) {
		try {
			if (!DatabaseConnection.verifyEmail(token)) return new ResponseEntity<>("Cannot find that token!",
				HttpStatus.NOT_FOUND);
			return new ResponseEntity<>("Success!", HttpStatus.OK);
		} catch (SQLException e) {
			LOGGER.error("SQL Email Verification Error", e);
			return new ResponseEntity<>("Failure! Error recorded.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * @param user user
	 * @return true on success false otherwise, logs error
	 */
	private boolean insertUser(User user) {
		try {
			while (DatabaseConnection.getUser(user.internalId.toString()) != null)
				user.internalId = UUID.randomUUID();

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			if (!MailUtil.sendVerificationEmail(user.email, user.internalId.toString(), user.name)) return false;
			DatabaseConnection.addNewUser(user.name, user.email, encoder.encode(user.password), user.internalId.toString());
		} catch (SQLException e) {
			try {
				DatabaseConnection.removeVerifications(user.internalId.toString());
			} catch (SQLException ignored) {
			}
			LOGGER.error("Registration failed!", e);
			return false;
		}
		return true;
	}
}
