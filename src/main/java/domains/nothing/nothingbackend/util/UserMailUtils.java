package domains.nothing.nothingbackend.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.Settings;
import okhttp3.*;

import java.io.IOException;

public class UserMailUtils {
	public static boolean createUserEmail(String emailHandle, String password) throws IOException {
		RequestBody newEmailRequest = new FormBody.Builder()
			.add("email", emailHandle.toLowerCase() + "@nothing.domains")
			.add("password", password)
			.build();
		Request request = authenticatedRequest()
			.url("https://email.nothing.domains/admin/mail/users/add")
			.post(newEmailRequest)
			.build();
		return Main.OK_HTTP.newCall(request).execute().isSuccessful();
	}

	public static boolean userEmailExists(String emailHandle) throws IOException {
		Request requestJson = authenticatedRequest()
			.get()
			.url("https://email.nothing.domains/admin/mail/users?format=json")
			.build();
		try (Response response = Main.OK_HTTP.newCall(requestJson).execute()) {

			ArrayNode jsonArray = Main.MAPPER.readerFor(ArrayNode.class).readValue(response.body().byteStream());

			// I'm hoping java does the right thing here, according to stackoverflow it *should* escape the loop AND return for the method.
			// Arsen's edit: of course it does, why did I even hire you?
			ArrayNode users = (ArrayNode) jsonArray.get(1).get("users");
			for (int i = 0; i < users.size(); i++) {
				String email = users.get(i).get("email").asText();
				if (email.equals(emailHandle.toLowerCase() + "@nothing.domains"))
					return true;
					response.close();
			}
			response.close();
		}
		return false;

	}

	private static Request.Builder authenticatedRequest() { // I wish this was Kotlin
		return new Request.Builder()
			.addHeader("Authorization",
				Credentials.basic(Settings.MAILINABOX_USER.get(), Settings.MAILINABOX_PASSWORD.get()));
	}
}
