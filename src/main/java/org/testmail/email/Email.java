package org.testmail.email;

import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testmail.email.EmailUtils.extractEmailAddresses;
import static org.testmail.email.EmailUtils.checkNonEmptyArgument;
import static org.testmail.email.EmailUtils.Property.DEFAULT_BCC_ADDRESS;
import static org.testmail.email.EmailUtils.Property.DEFAULT_BCC_NAME;
import static org.testmail.email.EmailUtils.Property.DEFAULT_CC_ADDRESS;
import static org.testmail.email.EmailUtils.Property.DEFAULT_CC_NAME;
import static org.testmail.email.EmailUtils.Property.DEFAULT_FROM_ADDRESS;
import static org.testmail.email.EmailUtils.Property.DEFAULT_FROM_NAME;
import static org.testmail.email.EmailUtils.Property.DEFAULT_REPLYTO_ADDRESS;
import static org.testmail.email.EmailUtils.Property.DEFAULT_REPLYTO_NAME;
import static org.testmail.email.EmailUtils.Property.DEFAULT_SUBJECT;
import static org.testmail.email.EmailUtils.Property.DEFAULT_TO_ADDRESS;
import static org.testmail.email.EmailUtils.Property.DEFAULT_TO_NAME;
import static org.testmail.email.EmailUtils.getProperty;
import static org.testmail.email.EmailUtils.hasProperty;


public class Email {

	/**
	 * Optional ID
	 */
	private String id;

	/**
	 * The sender of the email
	 */
	private Recipient fromRecipient;
	/**
	 * The reply-to-address, optional.
	 */
	private Recipient replyToRecipient;

	/**
	 * The email message body in plain text.
	 */
	private String text;

	/**
	 * The email message body in html.
	 */
	private String textHTML;

	/**
	 * The subject of the email message.
	 */
	private String subject;

	/**
	 * List of {@link Recipient}.
	 */
	private final List<Recipient> recipients;

	/**
	 * Map of header name and values.
	 */
	private final Map<String, String> headers;

	private boolean useReturnReceiptTo;
	
	/**
	 * @see #useReturnReceiptTo
	 */
	private Recipient returnReceiptTo;

	/**
	 * Constructor, creates all internal lists. Populates default from, reply-to, to, cc and bcc if provided in the config file.
	 */
	public Email() {
		this(true);
	}

	public Email(final boolean readFromDefaults) {
		recipients = new ArrayList<>();
		headers = new HashMap<>();

		if (readFromDefaults) {
			if (hasProperty(DEFAULT_FROM_ADDRESS)) {
				setFromAddress((String) getProperty(DEFAULT_FROM_NAME), (String) getProperty(DEFAULT_FROM_ADDRESS));
			}
			if (hasProperty(DEFAULT_REPLYTO_ADDRESS)) {
				setReplyToAddress((String) getProperty(DEFAULT_REPLYTO_NAME), (String) getProperty(DEFAULT_REPLYTO_ADDRESS));
			}
			if (hasProperty(DEFAULT_TO_ADDRESS)) {
				if (hasProperty(DEFAULT_TO_NAME)) {
					addNamedToRecipients((String) getProperty(DEFAULT_TO_NAME), (String) getProperty(DEFAULT_TO_ADDRESS));
				} else {
					addToRecipients((String) getProperty(DEFAULT_TO_ADDRESS));
				}
			}
			if (hasProperty(DEFAULT_CC_ADDRESS)) {
				if (hasProperty(DEFAULT_CC_NAME)) {
					addNamedCcRecipients((String) getProperty(DEFAULT_CC_NAME), (String) getProperty(DEFAULT_CC_ADDRESS));
				} else {
					addCcRecipients((String) getProperty(DEFAULT_CC_ADDRESS));
				}
			}
			if (hasProperty(DEFAULT_BCC_ADDRESS)) {
				if (hasProperty(DEFAULT_BCC_NAME)) {
					addNamedBccRecipients((String) getProperty(DEFAULT_BCC_NAME), (String) getProperty(DEFAULT_BCC_ADDRESS));
				} else {
					addBccRecipients((String) getProperty(DEFAULT_BCC_ADDRESS));
				}
			}
			if (hasProperty(DEFAULT_SUBJECT)) {
				setSubject((String) getProperty(DEFAULT_SUBJECT));
			}
		}
	}

	public void setId(final String id) {
		this.id = id;
	}
	
	/**
	 * Sets the sender address.
	 *
	 * @param name        The sender's name.
	 * @param fromAddress The sender's email address, mandatory.
	 */
	public void setFromAddress(final String name, final String fromAddress) {
		fromRecipient = new Recipient(name, checkNonEmptyArgument(fromAddress, "fromAddress"), null);
	}
	
	/**
	 * Sets the sender address from a preconfigured
	 *
	 * @param recipient The Recipient optional name and mandatory address.
	 */
	public void setFromAddress(Recipient recipient) {
		fromRecipient = new Recipient(recipient.getName(), checkNonEmptyArgument(recipient.getAddress(), "fromAddress"), null);
	}

	/**
	 * Sets the reply-to address (optional).
	 *
	 * @param name           The replied-to-receiver name.
	 * @param replyToAddress The replied-to-receiver email address.
	 */
	public void setReplyToAddress(final String name, final String replyToAddress) {
		replyToRecipient = new Recipient(name, checkNonEmptyArgument(replyToAddress, "replyToAddress"), null);
	}
	
	/**
	 * Sets the reply-to address from a preconfigured {@link Recipient} object..
	 *
	 * @param recipient The Recipient optional name and mandatory address.
	 */
	public void setReplyToAddress(Recipient recipient) {
		replyToRecipient = new Recipient(recipient.getName(), checkNonEmptyArgument(recipient.getAddress(), "replyToAddress"), null);
	}

	/**
	 * Bean setter for {@link #subject}.
	 */
	public void setSubject(final String subject) {
		this.subject = checkNonEmptyArgument(subject, "subject");
	}

	/**
	 * Bean setter for {@link #useReturnReceiptTo}.
	 */
	public void setUseReturnReceiptTo(boolean useReturnReceiptTo) {
		this.useReturnReceiptTo = useReturnReceiptTo;
	}
	
	/**
	 * Bean setter for {@link #returnReceiptTo}.
	 */
	public void setReturnReceiptTo(Recipient returnReceiptTo) {
		setUseReturnReceiptTo(true);
		this.returnReceiptTo = returnReceiptTo;
	}
	
	/**
	 * Bean setter for {@link #text}.
	 */
	public void setText(final String text) {
		this.text = text;
	}

	/**
	 * Bean setter for {@link #textHTML}.
	 */
	public void setTextHTML(final String textHTML) {
		this.textHTML = textHTML;
	}
	
	/**
	 * Delegates to {@link #addRecipients(String, RecipientType, String...)}, using empty default name and {@link RecipientType#TO}.
	 */
	public void addToRecipients(final String... delimitedEmailAddresses) {
		checkNonEmptyArgument(delimitedEmailAddresses, "emailAddressList");
		addRecipients(null, RecipientType.TO, delimitedEmailAddresses);
	}
	
	/**
	 * Delegates to {@link #addRecipients(String, RecipientType, String...)}, using empty default name and {@link RecipientType#CC}.
	 */
	public void addCcRecipients(final String... delimitedEmailAddresses) {
		checkNonEmptyArgument(delimitedEmailAddresses, "emailAddressList");
		addRecipients(null, RecipientType.CC, delimitedEmailAddresses);
	}
	
	/**
	 * Delegates to {@link #addRecipients(String, RecipientType, String...)}, using empty default name and {@link RecipientType#BCC}.
	 */
	public void addBccRecipients(final String... delimitedEmailAddresses) {
		checkNonEmptyArgument(delimitedEmailAddresses, "emailAddressList");
		addRecipients(null, RecipientType.BCC, delimitedEmailAddresses);
	}
	
	/**
	 * Delegates to {@link #addRecipients(String, RecipientType, String...)}, using {@link RecipientType#TO}.
	 */
	public void addNamedToRecipients(final String name, final String... delimitedEmailAddresses) {
		checkNonEmptyArgument(delimitedEmailAddresses, "emailAddressList");
		addRecipients(name, RecipientType.TO, delimitedEmailAddresses);
	}
	
	/**
	 * Delegates to {@link #addRecipients(String, RecipientType, String...)}, using {@link RecipientType#CC}.
	 */
	public void addNamedCcRecipients(final String name, final String... delimitedEmailAddresses) {
		checkNonEmptyArgument(delimitedEmailAddresses, "emailAddressList");
		addRecipients(name, RecipientType.CC, delimitedEmailAddresses);
	}
	
	/**
	 * Delegates to {@link #addRecipients(String, RecipientType, String...)}, using {@link RecipientType#BCC}.
	 */
	public void addNamedBccRecipients(final String name, final String... delimitedEmailAddresses) {
		checkNonEmptyArgument(delimitedEmailAddresses, "emailAddressList");
		addRecipients(name, RecipientType.BCC, delimitedEmailAddresses);
	}
	
	/**
	 * Adds all given recipients addresses to the list on account of address and recipient type (eg. {@link RecipientType#CC}).
	 * <p>
	 * Email address can be of format {@code "address@domain.com[,;*]"} or {@code "Recipient Name <address@domain.com>[,;*]"}. Included names would
	 * override the default recipientName provided.
	 *
	 * @param recipientName                The optional name to use for each email address in the {@code recipientEmailAddressesToAdd}.
	 * @param recipientEmailAddressesToAdd List of (preconfigured) recipients (with or without names, overriding the default name if included).
	 * @see #recipients
	 * @see Recipient
	 * @see RecipientType
	 */
	public void addRecipients(final String recipientName, final RecipientType type, final String... recipientEmailAddressesToAdd) {
		checkNonEmptyArgument(type, "type");
		checkNonEmptyArgument(recipientEmailAddressesToAdd, "recipientEmailAddressesToAdd");
		for (final String potentiallyCombinedEmailAddress : recipientEmailAddressesToAdd) {
			for (final String emailAddress : extractEmailAddresses(potentiallyCombinedEmailAddress)) {
				recipients.add(interpretRecipientData(recipientName, emailAddress, type));
			}
		}
	}
	
	static Recipient interpretRecipientData(String recipientName, String emailAddress, RecipientType type) {
		try {
			InternetAddress parsedAddress = InternetAddress.parse(emailAddress, false)[0];
			String relevantName = parsedAddress.getPersonal() != null ? parsedAddress.getPersonal() : recipientName;
			return new Recipient(relevantName, parsedAddress.getAddress(), type);
		} catch (AddressException e) {
			return new Recipient(recipientName, emailAddress, type);
		}
	}

	/**
	 * Adds all given {@link Recipient} instances to the list (as copies) on account of name, address and recipient type (eg. {@link RecipientType#CC}).
	 *
	 * @param recipientsToAdd List of preconfigured recipients.
	 * @see #recipients
	 * @see Recipient
	 * @see RecipientType
	 */
	public void addRecipients(final Recipient... recipientsToAdd) {
		for (final Recipient recipient : checkNonEmptyArgument(recipientsToAdd, "recipientsToAdd")) {
			final String address = checkNonEmptyArgument(recipient.getAddress(), "recipient.address");
			final RecipientType type = checkNonEmptyArgument(recipient.getType(), "recipient.type");
			recipients.add(new Recipient(recipient.getName(), address, type));
		}
	}

	/**
	 * Adds a header to the {@link #headers} list. The value is stored as a <code>String</code>. example: <code>email.addHeader("X-Priority",
	 * 2)</code>
	 *
	 * @param name  The name of the header.
	 * @param value The value of the header, which will be stored using {@link String#valueOf(Object)}.
	 */
	@SuppressWarnings("WeakerAccess")
	public void addHeader(final String name, final Object value) {
		checkNonEmptyArgument(name, "name");
		checkNonEmptyArgument(value, "value");
		headers.put(name, String.valueOf(value));
	}

	/**
	 * Bean getter for {@link #id}.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Bean getter for {@link #fromRecipient}.
	 */
	public Recipient getFromRecipient() {
		return fromRecipient;
	}

	/**
	 * Bean getter for {@link #replyToRecipient}.
	 */
	public Recipient getReplyToRecipient() {
		return replyToRecipient;
	}

	/**
	 * Bean getter for {@link #subject}.
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * Bean getter for {@link #useReturnReceiptTo}.
	 */
	public boolean isUseReturnReceiptTo() {
		return useReturnReceiptTo;
	}
	
	/**
	 * Bean getter for {@link #returnReceiptTo}.
	 */
	public Recipient getReturnReceiptTo() {
		return returnReceiptTo;
	}
	
	/**
	 * Bean getter for {@link #text}.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Bean getter for {@link #textHTML}.
	 */
	public String getTextHTML() {
		return textHTML;
	}

	/**
	 * Bean getter for {@link #recipients} as unmodifiable list.
	 */
	public List<Recipient> getRecipients() {
		return Collections.unmodifiableList(recipients);
	}

	/**
	 * Bean getter for {@link #headers} as unmodifiable map.
	 */
	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}


	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(final Object o) {
		return (this == o) || ((o != null) && (getClass() == o.getClass()) &&
				EqualsHelper.equalsEmail(this, (Email) o));
	}

	@Override
	public String toString() {
		String s = "Email{" +
				"\n\tid=" + id +
				"\n\tfromRecipient=" + fromRecipient +
				",\n\treplyToRecipient=" + replyToRecipient +
				",\n\ttext='" + text + '\'' +
				",\n\ttextHTML='" + textHTML + '\'' +
				",\n\tsubject='" + subject + '\'' +
				",\n\trecipients=" + recipients;
		if (useReturnReceiptTo) {
			s += ",\n\tuseReturnReceiptTo=" + true +
					",\n\t\treturnReceiptTo=" + returnReceiptTo;
		}
		if (!headers.isEmpty()) {
			s += ",\n\theaders=" + headers;
		}
		s += "\n}";
		return s;
	}

	/**
	 * Constructor for the Builder class
	 *
	 * @param builder The builder from which to create the email.
	 */
	Email(final EmailBuilder builder) {
		checkNonEmptyArgument(builder, "builder");
		recipients = builder.getRecipients();
		headers = builder.getHeaders();

		id = builder.getId();
		fromRecipient = builder.getFromRecipient();
		replyToRecipient = builder.getReplyToRecipient();
		text = builder.getText();
		textHTML = builder.getTextHTML();
		subject = builder.getSubject();

		useReturnReceiptTo = builder.isUseReturnReceiptTo();

		returnReceiptTo = builder.getReturnReceiptTo();
		
		if (useReturnReceiptTo) {

				if (builder.getReplyToRecipient() != null) {
					returnReceiptTo = builder.getReplyToRecipient();
				} else {
					returnReceiptTo = builder.getFromRecipient();
				}
		}
	}
}