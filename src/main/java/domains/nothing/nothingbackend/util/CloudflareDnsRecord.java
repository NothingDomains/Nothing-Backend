package domains.nothing.nothingbackend.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudflareDnsRecord {
	@JsonProperty
	String id;
	@JsonProperty
	String type;
	@JsonProperty
	String name;
	@JsonProperty
	String content;
	@JsonProperty
	boolean proxied;
	@JsonProperty
	String zone_id;
	@JsonProperty
	String zone_name;
}
