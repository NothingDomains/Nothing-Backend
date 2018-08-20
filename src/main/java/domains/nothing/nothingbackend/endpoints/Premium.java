package domains.nothing.nothingbackend.endpoints;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.objects.DNSRecord;
import domains.nothing.nothingbackend.objects.User;
import domains.nothing.nothingbackend.objects.Zone;
import domains.nothing.nothingbackend.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;

@GetFuckingRatelimitedBitch
@RestController
@RequestMapping("/api/premium")
public class Premium {


	@GetFuckingRatelimitedBitch
	@GetMapping("/buy")
	public ResponseEntity<JsonNode> buyPremium(HttpServletRequest req, HttpServletResponse res) throws SQLException {
		User user = DatabaseConnection.getUser(req.getSession().getAttribute("user").toString());
		if(!Utils.isLoggedIn(req)) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You're not logged in!"), HttpStatus.UNAUTHORIZED);
		}

		if(DatabaseConnection.isPremium(user.getUuid().toString())) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You already have Premium! Thank you for supporting us :D"), HttpStatus.BAD_REQUEST);

		}

		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", true)
				.put("message", "Please pay $5 to premium@nothing.domains using PayPal, with a note specifying your registered email address, which is: " + user.getEmail()), HttpStatus.OK);

	}




	@GetFuckingRatelimitedBitch
	@GetMapping("/email/create")
	public ResponseEntity<JsonNode> newEmail(HttpServletRequest req, HttpServletResponse response) throws SQLException, IOException {
		if (!Utils.isLoggedIn(req))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		User user = DatabaseConnection.getUser(req.getSession().getAttribute("user").toString());
		if (!DatabaseConnection.isPremium(user.getUuid().toString()))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You are not a premium member!"), HttpStatus.UNAUTHORIZED);
		if (UserMailUtils.userEmailExists(user.getName()))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "This email account already exists!"), HttpStatus.CONFLICT);
		String pass = Utils.randomString(15);

		if (!UserMailUtils.createUserEmail(user.getName(), pass))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "Internal Error."), HttpStatus.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("success", true)
			.put("password", pass)
			.put("username", user.getName() + "@nothing.domains"), HttpStatus.OK);
	}


	static class EDNSRecord {
		@JsonProperty
		String type;
		@JsonProperty
		String name;
		@JsonProperty
		String target;
		@JsonProperty
		String domain;
		@JsonProperty
		boolean proxied;
	}

	@SuppressWarnings("Duplicates")
	@GetFuckingRatelimitedBitch
	@PostMapping("/dns/create")
	public ResponseEntity<JsonNode> createRecord(@Valid @RequestBody EDNSRecord record, HttpServletRequest req, HttpServletResponse response) throws SQLException, IOException {
		if (!Utils.isLoggedIn(req))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		User user = DatabaseConnection.getUser(req.getSession().getAttribute("user").toString());
		if (!DatabaseConnection.isPremium(user.getUuid().toString()))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You are not a premium member!"), HttpStatus.UNAUTHORIZED);
		if(DatabaseConnection.getRecordTally(user.getUuid().toString()) >= 2) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You have hit your maximum record count of 2."), HttpStatus.UNAUTHORIZED);
		}
		RecordType recordType;
		try {
			recordType = RecordType.valueOf(record.type);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You specified an incorrect record type!"), HttpStatus.BAD_REQUEST);
		}

		Zone correctZone = CloudflareUtils.getZoneFromDomain(record.domain);
		if (correctZone == null) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "That domain doesn't exist!"),
				HttpStatus.BAD_REQUEST);
		}

		DNSRecord dnsRecord = new DNSRecord()
			.setRecord(record.name)
			.setContent(InetAddress.getByName(record.target))
			.setProxied(record.proxied)
			.setZoneId(correctZone.id);

		DatabaseConnection.addRecordTally(user.getUuid().toString());

		if (CloudflareUtils.createDnsRecord(recordType, dnsRecord)) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", true), HttpStatus.OK);
		}

		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("success", false)
			.put("error", "Internal Error"), HttpStatus.INTERNAL_SERVER_ERROR);

	}


	@GetFuckingRatelimitedBitch
	@PostMapping("/dns/update")
	public ResponseEntity<JsonNode> updateRecord(@Valid @RequestBody EDNSRecord record, HttpServletRequest req, HttpServletResponse response) throws SQLException, IOException {
		if (!Utils.isLoggedIn(req))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		User user = DatabaseConnection.getUser(req.getSession().getAttribute("user").toString());
		if (!DatabaseConnection.isPremium(user.getUuid().toString()))
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You are not a premium member!"), HttpStatus.UNAUTHORIZED);

		if(DatabaseConnection.getRecordTally(user.getUuid().toString()) >= 2) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You have hit your maximum record count of 2."), HttpStatus.UNAUTHORIZED);
		}

		RecordType recordType;
		try {
			recordType = RecordType.valueOf(record.type);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "You specified an incorrect record type!"), HttpStatus.BAD_REQUEST);
		}

		Zone correctZone = CloudflareUtils.getZoneFromDomain(record.domain);
		if (correctZone == null) {
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", false)
				.put("error", "That domain doesn't exist!"),
				HttpStatus.BAD_REQUEST);
		}

		DNSRecord dnsRecord = new DNSRecord()
			.setRecord(record.name)
			.setContent(InetAddress.getByName(record.target))
			.setProxied(record.proxied)
			.setZoneId(correctZone.id);

		if (CloudflareUtils.updateDnsRecord(recordType, dnsRecord)) {
			DatabaseConnection.addRecordTally(user.getUuid().toString());
			return new ResponseEntity<>(Main.MAPPER.createObjectNode()
				.put("success", true), HttpStatus.OK);
		}

		return new ResponseEntity<>(Main.MAPPER.createObjectNode()
			.put("success", false)
			.put("error", "Internal Error"), HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@GetFuckingRatelimitedBitch
	@PostMapping("/dns/delete")
	public ResponseEntity<JsonNode> /* end my suffering */ deleteRecord(@Valid @RequestBody DNSRecord record, HttpServletRequest req, HttpServletResponse response) throws SQLException, IOException {
		return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
	}
}
