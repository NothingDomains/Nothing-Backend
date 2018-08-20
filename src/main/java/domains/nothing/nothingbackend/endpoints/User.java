package domains.nothing.nothingbackend.endpoints;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import domains.nothing.nothingbackend.util.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@GetFuckingRatelimitedBitch
@RestController
@RequestMapping("/api/user")
public class User {

	static class LoginForm {
		@JsonProperty
		String username;
		@JsonProperty
		String email;
		@JsonProperty
		String npassword;
		@JsonProperty
		String cpassword;
	}


	@GetFuckingRatelimitedBitch
	@PostMapping("/update/email")
	public ResponseEntity<JsonNode> emailUpdate(HttpServletRequest req, HttpServletResponse res, @RequestBody String email) throws SQLException {

		if(!Utils.isLoggedIn(req)) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
					.put("success", false)
					.put("error", "You aren't logged in"), HttpStatus.UNAUTHORIZED);
		}
		if(Utils.notEmpty(email) && Utils.isValidEmailAddress(email)) {
			DatabaseConnection.changeEmail(email, req.getSession().getAttribute("user").toString());
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
					.put("success", true), HttpStatus.OK);
		}


		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("sucess", false)
				.put("error", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@GetFuckingRatelimitedBitch
	@PostMapping("/update/username")
	public ResponseEntity<JsonNode> usernameUpdate(HttpServletRequest req, HttpServletResponse res, @RequestBody String username) throws SQLException {

		if(!Utils.isLoggedIn(req)) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You aren't logged in"), HttpStatus.UNAUTHORIZED);
		}
		if(Utils.notEmpty(username)) {
			DatabaseConnection.changeUsername(username, req.getSession().getAttribute("user").toString());
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", true), HttpStatus.OK);
		}


		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("sucess", false)
			.put("error", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
	}





}
