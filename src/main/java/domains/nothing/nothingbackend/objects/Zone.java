package domains.nothing.nothingbackend.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Zone {
	@JsonProperty
	public String id;
	@JsonProperty
	public String name;
	@JsonProperty
	public String status;
}
