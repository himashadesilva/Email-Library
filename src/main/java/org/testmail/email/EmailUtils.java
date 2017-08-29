package org.testmail.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

public final class EmailUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtils.class);

	private static final String DEFAULT_CONFIG_FILENAME = "testmail.properties";

	/**
	 * Initially try to load properties from "{@value #DEFAULT_CONFIG_FILENAME}".
	 *
	 * @see #loadProperties(String, boolean)
	 * @see #loadProperties(InputStream, boolean)
	 */
	private static final Map<Property, Object> RESOLVED_PROPERTIES = new HashMap<>();

	static {
		loadProperties(DEFAULT_CONFIG_FILENAME, false);
	}

	public enum Property {
		JAVAXMAIL_DEBUG("testmail.javaxmail.debug"),
		TRANSPORT_STRATEGY("testmail.transportstrategy"),
		SMTP_HOST("testmail.smtp.host"),
		SMTP_PORT("testmail.smtp.port"),
		SMTP_USERNAME("testmail.smtp.username"),
		SMTP_PASSWORD("testmail.smtp.password"),
		PROXY_HOST("testmail.proxy.host"),
		PROXY_PORT("testmail.proxy.port"),
		PROXY_USERNAME("testmail.proxy.username"),
		PROXY_PASSWORD("testmail.proxy.password"),
		PROXY_SOCKS5BRIDGE_PORT("testmail.proxy.socks5bridge.port"),
		DEFAULT_SUBJECT("testmail.defaults.subject"),
		DEFAULT_FROM_NAME("testmail.defaults.from.name"),
		DEFAULT_FROM_ADDRESS("testmail.defaults.from.address"),
		DEFAULT_REPLYTO_NAME("testmail.defaults.replyto.name"),
		DEFAULT_REPLYTO_ADDRESS("testmail.defaults.replyto.address"),
		DEFAULT_TO_NAME("testmail.defaults.to.name"),
		DEFAULT_TO_ADDRESS("testmail.defaults.to.address"),
		DEFAULT_CC_NAME("testmail.defaults.cc.name"),
		DEFAULT_CC_ADDRESS("testmail.defaults.cc.address"),
		DEFAULT_BCC_NAME("testmail.defaults.bcc.name"),
		DEFAULT_BCC_ADDRESS("testmail.defaults.bcc.address"),
		DEFAULT_POOL_SIZE("testmail.defaults.poolsize"),
		DEFAULT_SESSION_TIMEOUT_MILLIS("testmail.defaults.sessiontimeoutmillis"),
		TRANSPORT_MODE_LOGGING_ONLY("testmail.transport.mode.logging.only");

		private final String key;

		Property(final String key) {
			this.key = key;
		}

		public String key() {
			return key;
		}
	}

	public static synchronized boolean hasProperty(final Property property) {
		return !EmailUtils.valueNullOrEmpty(RESOLVED_PROPERTIES.get(property));
	}

	public static synchronized <T> T getProperty(final Property property) {
		//noinspection unchecked
		return (T) RESOLVED_PROPERTIES.get(property);
	}


	public static Map<Property, Object> loadProperties(final String filename, final boolean addProperties) {
		final InputStream input = EmailUtils.class.getClassLoader().getResourceAsStream(filename);
		if (input != null) {
			return loadProperties(input, addProperties);
		}
		LOGGER.debug("Property file not found on classpath, skipping config file");
		return new HashMap<>();
	}

	public static Map<Property, Object> loadProperties(final Properties properties, final boolean addProperties) {
		if (!addProperties) {
			RESOLVED_PROPERTIES.clear();
		}
		RESOLVED_PROPERTIES.putAll(readProperties(properties));
		return unmodifiableMap(RESOLVED_PROPERTIES);
	}

	public static synchronized Map<Property, Object> loadProperties(final InputStream inputStream, final boolean addProperties) {
		final Properties prop = new Properties();

		try {
			prop.load(EmailUtils.checkArgumentNotEmpty(inputStream, "InputStream was null"));
		} catch (final IOException e) {
			throw new IllegalStateException("error reading properties file from inputstream", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (final IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		if (!addProperties) {
			RESOLVED_PROPERTIES.clear();
		}
		RESOLVED_PROPERTIES.putAll(readProperties(prop));
		return unmodifiableMap(RESOLVED_PROPERTIES);
	}

	/**
	 * @return All properties in priority of System property > File properties.
	 */
	private static Map<Property, Object> readProperties(final Properties fileProperties) {
		final Properties filePropertiesLeft = new Properties();
		filePropertiesLeft.putAll(fileProperties);
		final Map<Property, Object> resolvedProps = new HashMap<>();
		for (final Property prop : Property.values()) {
			if (System.getProperty(prop.key) != null) {
				System.out.println(prop.key + ": " + System.getProperty(prop.key));
			}
			final Object asSystemProperty = parsePropertyValue(System.getProperty(prop.key));
			if (asSystemProperty != null) {
				resolvedProps.put(prop, asSystemProperty);
				filePropertiesLeft.remove(prop.key);
			} else {
				final Object asEnvProperty = parsePropertyValue(System.getenv().get(prop.key));
				if (asEnvProperty != null) {
					resolvedProps.put(prop, asEnvProperty);
					filePropertiesLeft.remove(prop.key);
				} else {
					final Object rawValue = filePropertiesLeft.remove(prop.key);
					if (rawValue != null) {
						if (rawValue instanceof String) {
							resolvedProps.put(prop, parsePropertyValue((String) rawValue));
						} else {
							resolvedProps.put(prop, rawValue);
						}
					}
				}
			}
		}

		if (!filePropertiesLeft.isEmpty()) {
			throw new IllegalArgumentException("unknown properties provided " + filePropertiesLeft);
		}

		return resolvedProps;
	}

	/**
	 * @return The property value in boolean, integer or as original string value.
	 */
	static Object parsePropertyValue(final String propertyValue) {
		if (propertyValue == null) {
			return null;
		}
		// read boolean value
		final Map<String, Boolean> booleanConversionMap = new HashMap<>();
		booleanConversionMap.put("0", false);
		booleanConversionMap.put("1", true);
		booleanConversionMap.put("false", false);
		booleanConversionMap.put("true", true);
		booleanConversionMap.put("no", false);
		booleanConversionMap.put("yes", true);
		if (booleanConversionMap.containsKey(propertyValue)) {
			return booleanConversionMap.get(propertyValue.toLowerCase());
		}
		// read number value
		try {
			return Integer.valueOf(propertyValue);
		} catch (final NumberFormatException nfe) {
			// ok, so not a number
		}

		// return value as is (which should be string)
		return propertyValue;
	}

	public static <T> T checkArgumentNotEmpty(final T value, final String msg) {
		if (valueNullOrEmpty(value)) {
			throw new IllegalArgumentException(msg);
		}
		return value;
	}

	public static <T> boolean valueNullOrEmpty(final T value) {
		return value == null ||
				(value instanceof String && ((String) value).isEmpty()) ||
				(value instanceof Collection && ((Collection<?>) value).isEmpty()) ||
				(value instanceof byte[] && ((byte[]) value).length == 0);
	}

	public static String[] extractEmailAddresses(final String emailAddressList) {
		return checkNonEmptyArgument(emailAddressList, "emailAddressList")
				.replaceAll("(@.*?>?)\\s*[,;]", "$1<|>")
				.replaceAll("<\\|>$", "") // remove trailing delimiter
				.split("\\s*<\\|>\\s*"); // split on delimiter including surround space
	}

	public static <T> T checkNonEmptyArgument(final T address,final String parameterName) {
		if (EmailUtils.valueNullOrEmpty(address)) {
			throw new IllegalArgumentException(format("%s is required", parameterName));
		}
		return address;
	}
}