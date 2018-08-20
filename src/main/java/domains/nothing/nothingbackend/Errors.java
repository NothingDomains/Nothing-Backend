package domains.nothing.nothingbackend;

import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class Errors implements ErrorController {
	private static final Logger LOGGER = LoggerFactory.getLogger(Errors.class);
	private ErrorAttributes errorAttributes;

	@Autowired
	public Errors(ErrorAttributes errorAttributes) {
		Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
		this.errorAttributes = errorAttributes;
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> {
			ErrorPage errorPage = new ErrorPage("/error");
			container.addErrorPages(errorPage);
		});
	}

	@RequestMapping("/error")
	public ResponseEntity<String> error(HttpServletRequest request) {
		ServletRequestAttributes sr = new ServletRequestAttributes(request);
		Map<String, Object> attr = errorAttributes.getErrorAttributes(sr, false);
		HttpStatus status;
		try {
			status = HttpStatus.valueOf((int) attr.get("status"));
		} catch (Exception ignored) {
			status = HttpStatus.NOT_FOUND;
		}

		if (Objects.equals("freemarker.template.TemplateNotFoundException", attr.get("exception")))
			status = HttpStatus.valueOf(404);
		Object tmp = attr.getOrDefault("message", "");
		String message = tmp instanceof String ? (String) tmp : "";
		if (message.contains("org.apache.tomcat.util.http.fileupload.FileUploadBase$SizeLimitExceededException"))
			status = HttpStatus.PAYLOAD_TOO_LARGE;
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put("status", status.value());
		dataModel.put("reason", status.getReasonPhrase());
		StringWriter writer = new StringWriter();
		HttpHeaders h = new HttpHeaders();

		try {
			Main.FREEMARKER_CFG.getTemplate("error.ftl").process(dataModel, writer);
			h.setContentType(MediaType.TEXT_HTML);
		} catch (IOException | TemplateException ex) {
			h.setContentType(MediaType.APPLICATION_JSON);
			LOGGER.error("Failed to template error page!", ex);
			try {
				Main.MAPPER.writeValue(writer, dataModel);
			} catch (IOException ignored) {
			}
		}

		return new ResponseEntity<>(writer.toString(), h, status);
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
