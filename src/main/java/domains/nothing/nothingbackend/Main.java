package domains.nothing.nothingbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.stripe.Stripe;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

@SpringBootApplication
@EnableRedisHttpSession
public class Main extends SpringBootServletInitializer {
	public static final ObjectMapper MAPPER = new ObjectMapper();
	public static final Configuration FREEMARKER_CFG = new Configuration(Configuration.VERSION_2_3_26);
	public static final OkHttpClient OK_HTTP = new OkHttpClient();
	public static final ObjectMapper PRETTY_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	public static final JedisPool JEDIS;

	static {
		JedisPoolConfig pc = new JedisPoolConfig();
		JEDIS = new JedisPool();
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Main.class);
	}

	public static void main(String[] args) throws Exception {
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		Settings.init(p);
		Class.forName("domains.nothing.nothingbackend.DatabaseConnection");

		/*if (argss.contains("--premium")) {
			User user = DatabaseConnection.getUserFromEmail(args[1]);
			DatabaseConnection.renewPremiumSubscription(user.getUuid().toString(), Integer.valueOf(args[2]));
			System.out.println("User: " + args[1] + " has been given Premium for " + String.valueOf(args[2]) + " days");
			System.exit(0);
		}*/

		FREEMARKER_CFG.setClassLoaderForTemplateLoading(Main.class.getClassLoader(), "templates");
		FREEMARKER_CFG.setDefaultEncoding("UTF-8");
		FREEMARKER_CFG.setOutputFormat(HTMLOutputFormat.INSTANCE);
		Stripe.apiKey = Settings.STRIPE_TOKEN.get();
		SpringApplication.run(Main.class, args);
	}
}
