package domains.nothing.nothingbackend.objects;

import java.util.UUID;

public class User {

	String name;
	String password;
	String email;
	UUID uuid;

	public User(String userId, String name, String password, String email) {
		this.name = name;
		this.password = password;
		this.email = email;
		uuid = UUID.fromString(userId);
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		if (!name.equals(user.name)) return false;
		if (!password.equals(user.password)) return false;
		if (!email.equals(user.email)) return false;
		return uuid.equals(user.uuid);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + password.hashCode();
		result = 31 * result + email.hashCode();
		result = 31 * result + uuid.hashCode();
		return result;
	}

	public UUID getUuid() {
		return uuid;
	}
}
