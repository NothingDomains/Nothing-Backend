package domains.nothing.nothingbackend.util;

import com.stripe.exception.*;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.objects.User;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StripeUtils {


	public static Customer createCustomer(HttpServletRequest req, String chargeToken) throws SQLException, CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
		User u = DatabaseConnection.getUser((String) req.getSession().getAttribute("user"));

		//Customer info
		Map<String, Object> customerParams = new HashMap<>();
		customerParams.put("description", "Username: " + u.getName());
		customerParams.put("email", u.getEmail());
		//This contains the payment info
		customerParams.put("source", chargeToken);
		Customer cust = Customer.create(customerParams);

		DatabaseConnection.setCustomerId(cust.getId(), u.getUuid().toString());

		return cust;
	}

	public static Subscription subscribeToPremium(Customer customer) throws APIException, AuthenticationException, InvalidRequestException, APIConnectionException, CardException {
		//Make a premium plan "item"
		Map<String, Object> item = new HashMap<>();
		item.put("plan", "premium");

		//Add the items to the "cart"
		Map<String, Object> items = new HashMap<>();
		items.put("0", item);

		//Buy(subscribe) the items in the "cart"
		Map<String, Object> params = new HashMap<>();
		params.put("customer", customer.getId());
		params.put("items", items);

		//We don't set premium here because there still could be a card exception :(
		return Subscription.create(params);
	}


}
