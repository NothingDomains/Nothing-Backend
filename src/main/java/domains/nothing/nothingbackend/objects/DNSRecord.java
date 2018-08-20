package domains.nothing.nothingbackend.objects;

import java.net.InetAddress;

public class DNSRecord {
	private String record, zoneId;
	private InetAddress content;
	private boolean proxied = true;
	private String recordId;

	public DNSRecord setRecord(String record) {
		this.record = record;
		return this;
	}

	public DNSRecord setZoneId(String zoneId) {
		this.zoneId = zoneId;
		return this;
	}

	public DNSRecord setContent(InetAddress content) {
		this.content = content;
		return this;
	}

	public DNSRecord setProxied(boolean proxied) {
		this.proxied = proxied;
		return this;
	}

	public String getZoneId() {
		return zoneId;
	}

	public String getRecord() {
		return record;
	}

	public InetAddress getContent() {
		return content;
	}

	public boolean isProxied() {
		return proxied;
	}

	public String getRecordId() {
		return recordId;
	}
}
