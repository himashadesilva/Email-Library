package org.testmail.email;

import java.util.List;

final class EqualsHelper {

    public static boolean equalsEmail(final Email email1, final Email email2) {
        if (email1.getFromRecipient() != null ? !isEqualRecipient(email1.getFromRecipient(), email2.getFromRecipient()) : email2.getFromRecipient() != null) {
            return false;
        }
        if (email1.getReplyToRecipient() != null ? !isEqualRecipient(email1.getReplyToRecipient(), email2.getReplyToRecipient()) :
                email2.getReplyToRecipient() != null) {
            return false;
        }
        if (email1.getText() != null ? !email1.getText().equals(email2.getText()) : email2.getText() != null) {
            return false;
        }
        if (email1.getTextHTML() != null ? !email1.getTextHTML().equals(email2.getTextHTML()) : email2.getTextHTML() != null) {
            return false;
        }
        if (email1.getSubject() != null ? !email1.getSubject().equals(email2.getSubject()) : email2.getSubject() != null) {
            return false;
        }

        if (!isEqualRecipientList(email1.getRecipients(), email2.getRecipients())) {
            return false;
        }
        if (!email1.getHeaders().equals(email2.getHeaders())) {
            return false;
        }
        if (email1.isUseReturnReceiptTo() != email2.isUseReturnReceiptTo()) {
            return false;
        }
        return email1.getReturnReceiptTo() != null ? email1.getReturnReceiptTo().equals(email2.getReturnReceiptTo()) : email2.getReturnReceiptTo() == null;
    }

    private static boolean isEqualRecipientList(final List<Recipient> recipients, final List<Recipient> otherRecipients) {
        if (recipients.size() != otherRecipients.size()) {
            return false;
        }
        for (final Recipient otherRecipient : otherRecipients) {
            if (!containsRecipient(recipients, otherRecipient)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsRecipient(final List<Recipient> recipients, final Recipient otherRecipient) {
        for (final Recipient recipient : recipients) {
            if (isEqualRecipient(recipient, otherRecipient)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEqualRecipient(final Recipient recipient, final Recipient otherRecipient) {
        final String name = recipient != null ? recipient.getName() : null;
        final String otherName = otherRecipient != null ? otherRecipient.getName() : null;
        if (name != null ? !name.equals(otherName) : otherName != null) {
            return false;
        }
        assert otherRecipient != null;
        assert recipient != null;
        if (!recipient.getAddress().equals(otherRecipient.getAddress())) {
            return false;
        }
        return recipient.getType() != null ? recipient.getType().equals(otherRecipient.getType()) : otherRecipient.getType() == null;
    }

}
