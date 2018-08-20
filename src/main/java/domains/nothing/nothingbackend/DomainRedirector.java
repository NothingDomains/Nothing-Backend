package domains.nothing.nothingbackend;

import domains.nothing.nothingbackend.util.GetFuckingRatelimitedBitch;
import domains.nothing.nothingbackend.util.Utils;
import io.github.bucket4j.*;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class DomainRedirector implements Filter {
	private static DomainRedirector INSTANCE;
	private static final ExpiringMap<String, Bucket> RATE_LIMITS = ExpiringMap.builder()
		.expiration(0b1010, TimeUnit.MINUTES)
		.expirationPolicy(ExpirationPolicy.ACCESSED)
		.build();
	private static final ExpiringMap<String, Long> RATE_LIMIT_COUNT = ExpiringMap.builder()
		.expiration(0b1010, TimeUnit.MINUTES)
		.expirationPolicy(ExpirationPolicy.ACCESSED)
		.build();
	private final Set<String> RATE_LIMIT_PATHS = ConcurrentHashMap.newKeySet();

	private Bucket createNewBucket() {
		return Bucket4j.builder()
			.addLimit(Bandwidth.classic(8,
				Refill.smooth(2, Duration.ofSeconds(1))))
			.build();
	}

	public DomainRedirector() throws ClassNotFoundException {
		//noinspection ResultOfMethodCallIgnored
		new File("data/").mkdirs();
		INSTANCE = this;
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(GetFuckingRatelimitedBitch.class));
		for (BeanDefinition bd : scanner.findCandidateComponents("domains")) {
			Class<?> c = Class.forName(bd.getBeanClassName());
			Annotation annotation = Arrays.stream(c.getAnnotations())
				.filter(a -> a.annotationType() == RequestMapping.class)
				.findFirst().orElse(null);
			String path = "";
			if (annotation != null) path = ((RequestMapping) annotation).value()[0];
			RATE_LIMIT_PATHS.add(path);
			for (Method m : c.getMethods()) {
				if (Arrays.stream(m.getAnnotations())
					.anyMatch(a -> a.annotationType().equals(GetFuckingRatelimitedBitch.class)))
					for (Annotation a : m.getAnnotations()) {
						if (a.annotationType() == RequestMapping.class) {
							for (String s : ((RequestMapping) a).value()) {
								RATE_LIMIT_PATHS.add(path + s);
							}
						} else if (a.annotationType() == GetMapping.class) {
							for (String s : ((GetMapping) a).value()) {
								RATE_LIMIT_PATHS.add(path + s);
							}
						} else if (a.annotationType() == PostMapping.class) {
							for (String s : ((PostMapping) a).value()) {
								RATE_LIMIT_PATHS.add(path + s);
							}
						} else if (a.annotationType() == PutMapping.class) {
							for (String s : ((PutMapping) a).value()) {
								RATE_LIMIT_PATHS.add(path + s);
							}
						} else if (a.annotationType() == PatchMapping.class) {
							for (String s : ((PatchMapping) a).value()) {
								RATE_LIMIT_PATHS.add(path + s);
							}
						} else if (a.annotationType() == DeleteMapping.class) {
							for (String s : ((DeleteMapping) a).value()) {
								RATE_LIMIT_PATHS.add(path + s);
							}
						}
					}
			}
		}
	}

	@Bean
	public Filter domainRedirectFilter() {
		return DomainRedirector.INSTANCE;
	}

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = ((HttpServletRequest) request);
			HttpServletResponse res = (HttpServletResponse) response;
			String host = req.getHeader("X-Forwarded-Host");
			if (host != null && !host.equals("nothing.domains")) {
				String path = Utils.removeAfterLast(req.getRequestURI(), '.');
				String image = path.substring(1);
				if (path.startsWith("/api")) {
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
					res.getWriter().append("Only available on standard domain");
					res.setContentType("text/plain");
					return;
				}
				if (!StringUtils.isAlphanumeric(image)
					|| (image.length() != 9 && // new url format
					image.length() != 7 && // old url format
					(image.length() != 8 && !image.startsWith("u")))) {
					res.sendRedirect("https://nothing.domains" + path);
					return;
				}
			}
			try (Jedis conn = Main.JEDIS.getResource()) {
				String ip = req.getHeader("CF-Connecting-IP");
				if (ip == null) ip = req.getRemoteAddr();
				long currentCount = RATE_LIMIT_COUNT.getOrDefault(ip, 0L);
				boolean ipbanExists = IPFiltering.isIpBanned(conn, ip);
				if (ipbanExists || currentCount > 4) {
					if (!ipbanExists) {
						IPFiltering.ban(conn, ip, "ratelimit-overload");
					}
					RATE_LIMIT_COUNT.remove(ip);
					res.setContentType("text/plain");
					res.setStatus(403);
					res.getWriter().append("ur banned kiddo");
					return;
				}
				if (RATE_LIMIT_PATHS.contains(req.getServletPath())) {
					ConsumptionProbe probe = RATE_LIMITS.computeIfAbsent(ip, i -> createNewBucket()).tryConsumeAndReturnRemaining(1);
					if (!probe.isConsumed()) {
						RATE_LIMIT_COUNT.put(ip, currentCount + 1);
						res.setContentType("text/plain");
						res.setHeader("X-Rate-Limit-Retry-After-Seconds",
							String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
						res.setStatus(429);
						res.getWriter().append("Too many requests");
						return;
					} else res.setHeader("X-Rate-Limit-Remaining", "" + probe.getRemainingTokens());
				}
			}
			if (host != null)
				DomainUsages.incrementDomainUsageForXForwardedHostHeader(host);
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {

	}
}
