package domains.nothing.nothingbackend.objects;

public class APIKey {
	private int usages;
	private String value;

	public int getUsages() {
		return usages;
	}

	public String getValue() {
		return value;
	}

	public APIKey(int usages, String value) {
		this.usages = usages;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		APIKey apiKey = (APIKey) o;

		if (usages != apiKey.usages) return false;
		return value.equals(apiKey.value);
	}

	@Override
	public int hashCode() {
		int result = usages;
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "APIKey{" +
			"usages=" + usages +
			", value='" + value + '\'' +
			'}';
	}
}
