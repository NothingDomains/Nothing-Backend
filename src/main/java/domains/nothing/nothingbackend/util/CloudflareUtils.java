package domains.nothing.nothingbackend.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import domains.nothing.nothingbackend.Main;
import domains.nothing.nothingbackend.Settings;
import domains.nothing.nothingbackend.objects.DNSRecord;
import domains.nothing.nothingbackend.objects.Zone;
import net.jodah.expiringmap.ExpiringMap;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class CloudflareUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(CloudflareUtils.class);
	static String apiKey = Settings.CLOUDFLARE_KEY.get();
	static String email = Settings.CLOUDFLARE_EMAIL.get();
	private static ExpiringMap<String, Object> CACHE = ExpiringMap.builder()
		.expiration(3, TimeUnit.MINUTES)
		.build();

	public static boolean createDnsRecord(RecordType type, DNSRecord record) throws IOException {
		Response r = Main.OK_HTTP.newCall(authenticatedRequest()
			.url("https://api.cloudflare.com/client/v4/zones/" + record.getZoneId() + "/dns_records") // I wish this was Kotlin
			.post(RequestBody.create(MediaType.parse("application/json"),
				Main.MAPPER.writeValueAsString(Main.MAPPER.createObjectNode()
					.put("type", type.toString())
					.put("name", record.getRecord())
					.put("content", type == RecordType.CNAME ? record.getContent().getHostName()
						: record.getContent().getHostAddress())
					.put("proxied", record.isProxied()))))
			.build())
			.execute();
		CreateDnsRecordResponse response = Main.MAPPER.readValue(r.body().string(), CreateDnsRecordResponse.class);  // fuck json in java
		r.close();
		if (response.success)
			return true;
		else {
			LOGGER.error("Errors creating DNS record " + record.getRecord() + ": " + Arrays.toString(response.errors));
			return false;
		}
	}

	public static boolean updateDnsRecord(RecordType type, DNSRecord record) throws IOException {
		Response r = Main.OK_HTTP.newCall(authenticatedRequest()
			.url("https://api.cloudflare.com/client/v4/zones/" + record.getZoneId() + "/dns_records/" + record.getRecordId()) // I wish this was Kotlin
			.put(RequestBody.create(MediaType.parse("application/json"),
				Main.MAPPER.writeValueAsString(Main.MAPPER.createObjectNode()
					.put("type", type.toString())
					.put("name", record.getRecord())
					.put("content", record.getContent().getHostAddress())
					.put("proxied", record.isProxied()))))
			.build())
			.execute();
		CreateDnsRecordResponse response = Main.MAPPER.readValue(r.body().string(), CreateDnsRecordResponse.class);  // fuck json in java
		r.close();
		if (response.success)
			return true;
		else {
			LOGGER.error("Errors updating DNS record " + record.getRecord() + ": " + Arrays.toString(response.errors));
			return false;
		}
	}

	public static boolean deleteDnsRecord(String record, String zone) throws IOException {
		Response r = Main.OK_HTTP.newCall(authenticatedRequest()
			.url("https://api.cloudflare.com/client/v4/zones/" + zone + "/dns_records/" + record) // I wish this was Kotlin
			.delete()
			.build())
			.execute();
		DeleteDnsRecordResponse response = Main.MAPPER.readValue(r.body().string(), DeleteDnsRecordResponse.class);  // fuck json in java
		r.close();
		if (response.success)
			return true;
		else {
			LOGGER.error("Errors deleting DNS record " + record + ": " + Arrays.toString(response.errors));
			return false;
		}
	}

	public static boolean isZoneWildcard(Zone zone) {
		return getDnsRecords(zone.id).stream()
			.anyMatch(record -> (record.name.equals("*." + zone.name) || record.name.equals("*"))
				&& record.content.equals("nothing.domains"));
	}

	private static Set<CloudflareDnsRecord> getDnsRecords(String zone) {
		//noinspection unchecked
		return (Set<CloudflareDnsRecord>) CACHE.computeIfAbsent("zone-records " + zone, k -> {
			try {
				Response r = Main.OK_HTTP.newCall(authenticatedRequest()
					.url("https://api.cloudflare.com/client/v4/zones/" + zone + "/dns_records")
					.get()
					.build()
				).execute();
				DnsRecordsResponse response = Main.MAPPER.readValue(r.body().string(), DnsRecordsResponse.class);
				r.close();
				if (!response.success) {
					LOGGER.error("Errors obtaining zone list: " + Arrays.toString(response.errors));
					return null;
				}
				return Arrays.stream(response.result).collect(Collectors.toSet());
			} catch (IOException e) {
				LOGGER.error("Failed to get zone list!", e);
				return null;
			}
		});
	}

	public static List<Zone> getZones() {
		//noinspection unchecked
		return (List<Zone>) CACHE.computeIfAbsent("zones", k -> {
			try {
				Response r = Main.OK_HTTP.newCall(
					authenticatedRequest()
						.url("https://api.cloudflare.com/client/v4/zones?per_page=50")
						.get()
						.build()
				).execute();
				ListZoneResponse response = Main.MAPPER.readValue(r.body().string(), ListZoneResponse.class);
				r.close();
				if (!response.success) {
					LOGGER.error("Errors obtaining zone list: " + Arrays.toString(response.errors));
					return null;
				}
				return Arrays.asList(response.result);
			} catch (IOException e) {
				LOGGER.error("Failed to get zone list!", e);
				return null;
			}
		});
	}

	public static Zone getZoneFromDomain(String domain) {
		List<Zone> zones = getZones();
		for (Zone zone : zones) {
			if (zone.name.equals(domain)) {
				return zone;
			}
		}
		return null;
	}


	private static Request.Builder authenticatedRequest() { // I wish this was Kotlin
		return new Request.Builder()
			.addHeader("X-Auth-Key", apiKey)
			.addHeader("X-Auth-Email", email)
			.addHeader("Content-Type", "application/json");

	}

	private static class Error {
		int code;
		String message;

		@Override
		public String toString() {
			return code + ": " + message;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class CreateDnsRecordResponse {
		@JsonProperty
		boolean success;
		@JsonProperty
		Error[] errors;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class ListZoneResponse {
		@JsonProperty
		boolean success;
		@JsonProperty
		Error[] errors;
		@JsonProperty
		Zone[] result;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class DeleteDnsRecordResponse {
		@JsonProperty
		boolean success;
		@JsonProperty
		Error[] errors;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class DnsRecordsResponse {
		@JsonProperty
		boolean success;
		@JsonProperty
		Error[] errors;
		@JsonProperty
		CloudflareDnsRecord[] result;
	}
}
