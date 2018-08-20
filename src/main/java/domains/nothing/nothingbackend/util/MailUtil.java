package domains.nothing.nothingbackend.util;

import domains.nothing.nothingbackend.DatabaseConnection;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.Settings;
import domains.nothing.nothingbackend.objects.User;
import freemarker.template.TemplateException;
import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.MailBuilder;
import net.sargue.mailgun.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MailUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);
	static Configuration config = new Configuration()
		.domain("mail.nothing.domains")
		.apiKey(Settings.MAIL_KEY.get())
		.from("Nothing Domains", "noreply@mail.nothing.domains");

	public static MailBuilder sendEmail(String title, String body) {
		StringWriter sw = new StringWriter();
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("body", body);
			data.put("sub", title);
			Main.FREEMARKER_CFG.getTemplate("email.ftl").process(data, sw);
		} catch (TemplateException | IOException e) {
			LOGGER.error("EMail Error", e);
			return null;
		}
		return Mail.using(config)
			.html(sw.toString())
			.subject(title);
	}

	private static final String iWishThisWasKotlin = "Hi, <br>\n" +
		"According to our records, you just signed up for a Nothing Domains account. To verify your account, " +
		"please click <a href=\"https://nothing.domains/api/register/verify/{TOKEN}\">here.</a><br>\n" +
		"If you didn't create a Nothing Domains account, you can safely ignore this email.<br>\n" +
		"Thanks,<br>\n" +
		"The Nothing Domains Team" +
		"<h6>In case the link above is not clickable, navigate to: https://nothing.domains/api/register/verify/{TOKEN}</h6>";

	public static boolean sendVerificationEmail(String email, String internalId, String name) throws SQLException {
		String verificationToken = UUID.randomUUID().toString();
		DatabaseConnection.insertVerificationEmail(internalId, verificationToken);

		MailBuilder mail = sendEmail("Registration Confirmation",
			iWishThisWasKotlin.replace("{TOKEN}", verificationToken));
		boolean ok = mail != null && mail.to(name, email).build().send().isOk();
		if (!ok) {
			LOGGER.error("Mailgun failure");
			DatabaseConnection.removeVerifications(internalId);
		}
		return ok;
	}


	private static final String ireallywannadie = "Hi, \n" +
		"Your premium subscription has expired! Please send your $5 fee to premium@nothing.domains on PayPal, and then ask for it to be re-assigned." +
		"Thanks,\nThe Nothing Domains Team";

	public static boolean sendPremiumExpiredEmail(String email, String name) {
		MailBuilder mail = sendEmail("Nothing Domains | Your premium has expired!", ireallywannadie);
		boolean ok = mail != null && mail.to(name, email).build().send().isOk();
		if (!ok) {
			LOGGER.error("Mailgun failure");
		}
		return ok;
	}

	private static final String iReallyWishThisWasKotlinForDamnSakePONCEYOULAZY = "Hi, <br>\n" +
		"According to our records, you just asked for a password reset Nothing Domains account. To reset your password, " +
		"please click <a href=\"https://nothing.domains/reset/{TOKEN}\">here.</a><br>\n" +
		"If you didn't request a password reset, you can safely ignore this email, and the reset link will be invalid in 24h.<br>\n" +
		"Thanks,<br>\n" +
		"The Nothing Domains Team" +
		"<h6>In case the link above is not clickable, navigate to: https://nothing.domains/reset/{TOKEN}</h6>";

	public static boolean sendResetEmail(String userId) throws SQLException {
		User u;
		if ((u = DatabaseConnection.getUser(userId)) != null) {
			String passwordResetToken = UUID.randomUUID().toString();
			while (DatabaseConnection.resetKeyExists(passwordResetToken))
				passwordResetToken = UUID.randomUUID().toString();
			DatabaseConnection.insertPasswordReset(u.getUuid().toString(), passwordResetToken);
			MailBuilder mail = sendEmail("Password Reset",
				iReallyWishThisWasKotlinForDamnSakePONCEYOULAZY.replace("{TOKEN}", passwordResetToken));
			if (mail == null) {
				LOGGER.error("Mailgun failure");
				DatabaseConnection.removePasswordResets(userId);
				return false;
			}
			Response email = mail.to(u.getName(), u.getEmail()).build().send();
			boolean ok = email.isOk();
			if (!ok) {
				LOGGER.error("Mailgun failure! Message: {}", email.responseMessage());
				DatabaseConnection.removePasswordResets(userId);
				return false;
			}
			return true;
		} else return false;
	}

	private static final String newPremiumSubscriptionEmail = "Hi {USER}!<br>\n" +
		"Thank you so much for buying premium and supporting us! All of your premium benefits are accessible by logging into your Nothing Domains" +
		"account and selecting the \"Premium\" tab.\n" +
		"Appreciate the supports<br>\n" +
		"The Nothing Domains Team";
	public static boolean sendNewPremiumSubscriptionEmail(User user) {
		MailBuilder mail = sendEmail("Thank you for purchasing Nothing Domains Premium!", newPremiumSubscriptionEmail.replace("{USER}", user.getName()));
		if (mail == null) {
			LOGGER.error("Mailgun failure");
			return false;
		}
		Response email = mail.to(user.getName(), user.getEmail()).build().send();
		boolean ok = email.isOk();
		if(!ok) {
			LOGGER.error("Mailgun failure! Message: {}", email.responseMessage());
			return false;
		}
		return true;
	}

}
