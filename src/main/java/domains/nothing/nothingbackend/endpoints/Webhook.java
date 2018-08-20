package domains.nothing.nothingbackend.endpoints;


import com.fasterxml.jackson.databind.JsonNode;
import com.paypal.ipn.IPNMessage;
import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.objects.User;
import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import domains.nothing.nothingbackend.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/webhook")
public class Webhook {
	private static final Logger LOGGER = LoggerFactory.getLogger(Webhook.class);
	private static Map<String, String> ppConfigMap = new HashMap<>();
	static {
		ppConfigMap.put("mode", "live");
	}

	@PostMapping("/paypal")
	@GetFuckingRatelimitedBitch
	public ResponseEntity paypal(HttpServletRequest request, HttpServletResponse res) throws SQLException {
		IPNMessage ipnListener = new IPNMessage(request, ppConfigMap);
		LOGGER.info("New IPN message received from PayPal!");
		if (!ipnListener.validate()) {
			// In the future we'll ban them immediately
			return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		}
		switch(ipnListener.getTransactionType()) {
			case "subscr_signup":
				if(ipnListener.getIpnValue("item_name").equals("Premium")) {
					String uuid = ipnListener.getIpnValue("custom");
					User user = DatabaseConnection.getUser(uuid);
					LOGGER.info("User: " + user.getName() + " | " + user.getEmail() + " has purchased premium :D");
					DatabaseConnection.setCustomerId(ipnListener.getIpnValue("subscr_id"), uuid);
					DatabaseConnection.renewPremiumSubscription(uuid, 31);
					MailUtil.sendNewPremiumSubscriptionEmail(user);
					return new ResponseEntity(HttpStatus.OK);
				}

			case "subscr_payment":
				if(ipnListener.getIpnValue("item_name").equals("Premium")) {
					String uuid = DatabaseConnection.getUUIDFromCustomerId(ipnListener.getIpnValue("subscr_id"));
					DatabaseConnection.renewPremiumSubscription(uuid, 31);
					return new ResponseEntity(HttpStatus.OK);
				}

			case "subscr_failed":
				if(ipnListener.getIpnValue("item_name").equals("Premium")) {
					String uuid = DatabaseConnection.getUUIDFromCustomerId(ipnListener.getIpnValue("subscr_id"));
					User user = DatabaseConnection.getUser(uuid);
					// Send user email "premium expire@!! ! 1  1 "
					return new ResponseEntity(HttpStatus.OK);
				}

			default:
				return new ResponseEntity(HttpStatus.OK);
		}
	}

}
