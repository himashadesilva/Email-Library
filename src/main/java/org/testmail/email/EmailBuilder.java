package org.testmail.email;

import javax.mail.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class EmailBuilder {
	
	/**
	 * Optional ID
	 */
	private String id;
	
	private Recipient fromRecipient;
	
	/**
	 * The reply-to-address, optional. Can be used in conjunction with
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
	 * Map of header name and values, such as <code>X-Priority</code> etc.
	 */
	private final Map<String, String> headers;

	private boolean useReturnReceiptTo;
	
	/**
	 * @see #useReturnReceiptTo
	 */
	private Recipient returnReceiptTo;
	
	public EmailBuilder() {
		recipients = new ArrayList<>();
		headers = new HashMap<>();
		
		if (hasProperty(DEFAULT_FROM_ADDRESS)) {
			from((String) getProperty(DEFAULT_FROM_NAME), (String) getProperty(DEFAULT_FROM_ADDRESS));
		}
		if (hasProperty(DEFAULT_REPLYTO_ADDRESS)) {
			replyTo((String) getProperty(DEFAULT_REPLYTO_NAME), (String) getProperty(DEFAULT_REPLYTO_ADDRESS));
		}
		if (hasProperty(DEFAULT_TO_ADDRESS)) {
			if (hasProperty(DEFAULT_TO_NAME)) {
				to((String) getProperty(DEFAULT_TO_NAME), (String) getProperty(DEFAULT_TO_ADDRESS));
			} else {
				to((String) getProperty(DEFAULT_TO_ADDRESS));
			}
		}
		if (hasProperty(DEFAULT_CC_ADDRESS)) {
			if (hasProperty(DEFAULT_CC_NAME)) {
				cc((String) getProperty(DEFAULT_CC_NAME), (String) getProperty(DEFAULT_CC_ADDRESS));
			} else {
				cc((String) getProperty(DEFAULT_CC_ADDRESS));
			}
		}
		if (hasProperty(DEFAULT_BCC_ADDRESS)) {
			if (hasProperty(DEFAULT_BCC_NAME)) {
				bcc((String) getProperty(DEFAULT_BCC_NAME), (String) getProperty(DEFAULT_BCC_ADDRESS));
			} else {
				bcc((String) getProperty(DEFAULT_BCC_ADDRESS));
			}
		}
		if (hasProperty(DEFAULT_SUBJECT)) {
			subject((String) getProperty(DEFAULT_SUBJECT));
		}
	}
	
	public Email build() {
		return new Email(this);
	}
	
	/**
	 * Sets the optional id to be used when sending using the underlying Java Mail framework. Will be generated otherwise.
	 */
	public EmailBuilder id(final String id) {
		this.id = id;
		return this;
	}
	
	/**
	 * Sets the sender address {@link #fromRecipient}.
	 *
	 * @param name        The sender's name.
	 * @param fromAddress The sender's email address.
	 */
	public EmailBuilder from(final String name, final String fromAddress) {
		checkNonEmptyArgument(fromAddress, "fromAddress");
		this.fromRecipient = new Recipient(name, fromAddress, null);
		return this;
	}
	
	/**
	 * Sets the sender address {@link #fromRecipient} with preconfigured.
	 *
	 * @param recipient Preconfigured recipient (name is optional).
	 */
	public EmailBuilder from(final Recipient recipient) {
		checkNonEmptyArgument(recipient, "recipient");
		this.fromRecipient = new Recipient(recipient.getName(), recipient.getAddress(), null);
		return this;
	}
	
	/**
	 * Sets {@link #replyToRecipient} (optional).
	 *
	 * @param name           The replied-to-receiver name.
	 * @param replyToAddress The replied-to-receiver email address.
	 */
	public EmailBuilder replyTo(final String name, final String replyToAddress) {
		checkNonEmptyArgument(replyToAddress, "replyToAddress");
		this.replyToRecipient = new Recipient(name, replyToAddress, null);
		return this;
	}
	
	/**
	 * Sets {@link #replyToRecipient} (optional) with preconfigured {@link Recipient}.
	 *
	 * @param recipient Preconfigured recipient (name is optional).
	 */
	public EmailBuilder replyTo(final Recipient recipient) {
		checkNonEmptyArgument(recipient, "recipient");
		this.replyToRecipient = new Recipient(recipient.getName(), recipient.getAddress(), null);
		return this;
	}
	
	/**
	 * Sets the {@link #subject}.
	 */
	public EmailBuilder subject(final String subject) {
		this.subject = checkNonEmptyArgument(subject, "subject");
		return this;
	}
	
	/**
	 * Sets the {@link #text}.
	 */
	public EmailBuilder text(final String text) {
		this.text = text;
		return this;
	}
	
	/**
	 * Sets the {@link #textHTML}.
	 */
	public EmailBuilder textHTML(final String textHTML) {
		this.textHTML = textHTML;
		return this;
	}
	
	/**
	 * Adds new {@link Recipient} instances to the list on account of name, address with recipient type {@link Message.RecipientType#TO}.
	 *
	 * @param recipientsToAdd The recipients whose name and address to use
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilder to(final Recipient... recipientsToAdd) {
		for (final Recipient recipient : checkNonEmptyArgument(recipientsToAdd, "recipientsToAdd")) {
			recipients.add(new Recipient(recipient.getName(), recipient.getAddress(), Message.RecipientType.TO));
		}
		return this;
	}
	
	/**
	 * Delegates to {@link #to(String, String)} while omitting the name used for the recipient(s).
	 */
	public EmailBuilder to(final String emailAddressList) {
		return to(null, emailAddressList);
	}
	
	/**
	 * Adds anew {@link Recipient} instances to the list on account of given name, address with recipient type .
	 * List can be comma ',' or semicolon ';' separated.
	 *
	 * @param name             The name of the recipient(s).
	 * @param emailAddressList The emailaddresses of the recipients (will be singular in most use cases).
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilder to(final String name, final String emailAddressList) {
		checkNonEmptyArgument(emailAddressList, "emailAddressList");
		return addCommaOrSemicolonSeparatedEmailAddresses(name, emailAddressList, Message.RecipientType.TO);
	}

	private EmailBuilder addCommaOrSemicolonSeparatedEmailAddresses(final String name, final String emailAddressList, final Message.RecipientType type) {
		checkNonEmptyArgument(type, "type");
		for (final String emailAddress : EmailUtils.extractEmailAddresses(checkNonEmptyArgument(emailAddressList, "emailAddressList"))) {
			recipients.add(Email.interpretRecipientData(name, emailAddress, type));
		}
		return this;
	}
	
	/**
	 * Adds new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#TO}.
	 *
	 * @param emailAddresses The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilder to(final String... emailAddresses) {
		for (final String emailAddress : checkNonEmptyArgument(emailAddresses, "emailAddresses")) {
			recipients.add(new Recipient(null, emailAddress, Message.RecipientType.TO));
		}
		return this;
	}
	
	/**
	 * Adds new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#CC}.
	 *
	 * @param emailAddresses The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilder cc(final String... emailAddresses) {
		for (final String emailAddress : checkNonEmptyArgument(emailAddresses, "emailAddresses")) {
			recipients.add(new Recipient(null, emailAddress, Message.RecipientType.CC));
		}
		return this;
	}
	
	
	/**
	 * Delegates to {@link #cc(String, String)} while omitting the name for the CC recipient(s).
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilder cc(final String emailAddressList) {
		return cc(null, emailAddressList);
	}
	
	/**
	 * Adds anew {@link Recipient} instances to the list on account of empty name, address with recipient type. List can be
	 * comma ',' or semicolon ';' separated.
	 *
	 * @param name             The name of the recipient(s).
	 * @param emailAddressList The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilder cc(String name, final String emailAddressList) {
		checkNonEmptyArgument(emailAddressList, "emailAddressList");
		return addCommaOrSemicolonSeparatedEmailAddresses(name, emailAddressList, Message.RecipientType.CC);
	}
	
	/**
	 * Adds new {@link Recipient} instances to the list on account of name, address with recipient type {@link Message.RecipientType#CC}.
	 *
	 * @param recipientsToAdd The recipients whose name and address to use
	 * @see #recipients
	 * @see Recipient
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilder cc(final Recipient... recipientsToAdd) {
		for (final Recipient recipient : checkNonEmptyArgument(recipientsToAdd, "recipientsToAdd")) {
			recipients.add(new Recipient(recipient.getName(), recipient.getAddress(), Message.RecipientType.CC));
		}
		return this;
	}
	
	/**
	 * Adds new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#BCC}.
	 *
	 * @param emailAddresses The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilder bcc(final String... emailAddresses) {
		for (final String emailAddress : checkNonEmptyArgument(emailAddresses, "emailAddresses")) {
			recipients.add(new Recipient(null, emailAddress, Message.RecipientType.BCC));
		}
		return this;
	}
	
	/**
	 * Delegates to {@link #bcc(String, String)} while omitting the name for the BCC recipient(s).
	 */
	public EmailBuilder bcc(final String emailAddressList) {
		return bcc(null, emailAddressList);
	}
	
	/**
	 * Adds anew {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#BCC}. List can be
	 * comma ',' or semicolon ';' separated.
	 *
	 * @param name             The name of the recipient(s).
	 * @param emailAddressList The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilder bcc(String name, final String emailAddressList) {
		checkNonEmptyArgument(emailAddressList, "emailAddressList");
		return addCommaOrSemicolonSeparatedEmailAddresses(name, emailAddressList, Message.RecipientType.BCC);
	}
	
	/**
	 * Adds new {@link Recipient} instances to the list on account of name, address with recipient type {@link Message.RecipientType#BCC}.
	 *
	 * @param recipientsToAdd The recipients whose name and address to use
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilder bcc(final Recipient... recipientsToAdd) {
		for (final Recipient recipient : checkNonEmptyArgument(recipientsToAdd, "recipientsToAdd")) {
			recipients.add(new Recipient(recipient.getName(), recipient.getAddress(), Message.RecipientType.BCC));
		}
		return this;
	}

	/**
	 * Adds a header to the {@link #headers} list. The value is stored as a <code>String</code>. example: <code>email.addHeader("X-Priority",
	 * 2)</code>
	 *
	 * @param name  The name of the header.
	 * @param value The value of the header, which will be stored using {@link String#valueOf(Object)}.
	 */
	public EmailBuilder addHeader(final String name, final Object value) {
		checkNonEmptyArgument(name, "name");
		checkNonEmptyArgument(value, "value");
		headers.put(name, String.valueOf(value));
		return this;
	}

	/**
	 * Indicates that we want to use the flag {@link #returnReceiptTo}. The actual address will default to the {@link #replyToRecipient}
	 * first if set or else {@link #fromRecipient}.
	 */
	public EmailBuilder withReturnReceiptTo() {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = null;
		return this;
	}
	
	/**
	 * Indicates that we want to use the NPM flag {@link #returnReceiptTo} with the given mandatory address.
	 */
	public EmailBuilder withReturnReceiptTo(String address) {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = new Recipient(null, checkNonEmptyArgument(address, "returnReceiptToAddress"), null);
		return this;
	}
	
	/**
	 * Indicates that we want to use the NPM flag {@link #returnReceiptTo} with the given optional name and mandatory address.
	 */
	public EmailBuilder withReturnReceiptTo(String name, String address) {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = new Recipient(name, checkNonEmptyArgument(address, "returnReceiptToAddress"), null);
		return this;
	}
	
	/**
	 * Indicates that we want to use the NPM flag {@link #returnReceiptTo} with the preconfigured {@link Recipient}.
	 */
	public EmailBuilder withReturnReceiptTo(Recipient recipient) {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = new Recipient(recipient.getName(), checkNonEmptyArgument(recipient.getAddress(), "returnReceiptToAddress"), null);
		return this;
	}

	public String getId() {
		return id;
	}
	
	public Recipient getFromRecipient() {
		return fromRecipient;
	}
	
	public Recipient getReplyToRecipient() {
		return replyToRecipient;
	}
	
	public String getText() {
		return text;
	}
	
	public String getTextHTML() {
		return textHTML;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public List<Recipient> getRecipients() {
		return new ArrayList<>(recipients);
	}

	
	public Map<String, String> getHeaders() {
		return new HashMap<>(headers);
	}
	
	public boolean isUseReturnReceiptTo() {
		return useReturnReceiptTo;
	}
	
	public Recipient getReturnReceiptTo() {
		return returnReceiptTo;
	}
}