package domains.nothing.nothingbackend.endpoints;

import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.objects.User;
import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import domains.nothing.nothingbackend.util.MailUtil;
import domains.nothing.nothingbackend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.GET;
import java.io.IOException;
import java.sql.SQLException;

@GetFuckingRatelimitedBitch
@RequestMapping("/api/login")
@RestController
public class Login {
	private static final Logger LOGGER = LoggerFactory.getLogger(Login.class);

	@GetFuckingRatelimitedBitch
	@PostMapping
	public ResponseEntity<String> login(@Valid @RequestBody LoginForm login, HttpServletRequest request,
	                                    HttpServletResponse response) throws SQLException, IOException {
		if (!DatabaseConnection.accountUnverified(login.user))
			new ResponseEntity<>("Your e-mail address is not verified.", HttpStatus.OK);
		if (DatabaseConnection.userExists(login.user, login.password)) {
			//noinspection ConstantConditions
			request.getSession().setAttribute("user", DatabaseConnection.getUserFromLogin(login.user).getUuid().toString());
			return new ResponseEntity<>("Success", HttpStatus.OK);
		} else return new ResponseEntity<>("Bad email/password", HttpStatus.UNAUTHORIZED);
	}

	private static class LoginForm {
		public String user;
		public String password;
	}

	@GetFuckingRatelimitedBitch
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse res) throws IOException {
		try {
			request.getSession().invalidate();
		} catch (Exception ignored) {
		}
		res.sendRedirect("/");
		return "redirect:/";
	}

	@GetFuckingRatelimitedBitch
	@GetMapping("/reset/{token_or_email:.+}")
	public String verify(@PathVariable("token_or_email") String token, HttpServletRequest request,
	                     HttpServletResponse response) throws SQLException, IOException {
		if (Utils.isLoggedIn(request)) {
			response.sendRedirect("/");
			return "";
		}
		if (Utils.isValidEmailAddress(token)) {
			User user = DatabaseConnection.getUserFromEmail(token);
			if (user == null) return "No such email!";
			if (MailUtil.sendResetEmail(user.getUuid().toString())) return "Success!";
			return "Failure!";
		} else {
			return "That doesn't look like an email, perhaps you meant to POST the new password?";
		}
	}

	@GetFuckingRatelimitedBitch
	@PostMapping("/reset/{token_or_email:.+}")
	public String reset(@PathVariable("token_or_email") String token, HttpServletRequest request,
	                    @RequestBody String newPass, HttpServletResponse response) throws SQLException, IOException {
		if (Utils.isLoggedIn(request)) {
			response.sendRedirect("/");
			return "";
		}
		if (!Utils.isValidEmailAddress(token)) {
			try {
				if (DatabaseConnection.resetPassword(token, newPass)) return "Success!";
			} catch (IllegalArgumentException e) {
				response.setStatus(400);
				return e.getMessage();
			}
			response.setStatus(400);
			return "Wrong E-Mail!";
		} else {
			return "That looks like an email, perhaps you meant to use GET?";
		}
	}
}
