package org.testmail.email;

import javax.mail.Message.RecipientType;
import java.util.Objects;

public final class Recipient {

	private final String name;
	private final String address;
	private final RecipientType type;

	/**
	 * Constructor; initializes this recipient object.
	 * 
	 * @param name The name of the recipient, optional in which just the address is shown.
	 * @param address The email address of the recipient.
	 * @param type The recipient type (eg. {@link RecipientType#TO}), optional for {@code from} and {@code replyTo} fields.
	 * @see RecipientType
	 */
	public Recipient(final String name,final String address, final RecipientType type) {
		this.name = name;
		this.address = EmailUtils.checkNonEmptyArgument(address, "address");
		this.type = type;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Recipient recipient = (Recipient) o;
		return Objects.equals(name, recipient.name) &&
				Objects.equals(address, recipient.address) &&
				Objects.equals(type, recipient.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, address, type);
	}

	@Override public String toString() {
		return "Recipient{" +
				"name='" + name + '\'' +
				", address='" + address + '\'' +
				", type=" + type +
				'}';
	}


	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public RecipientType getType() {
		return type;
	}
}