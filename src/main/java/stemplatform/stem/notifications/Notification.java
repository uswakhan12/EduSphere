package stemplatform.stem.notifications;

import stemplatform.stem.users.Viewer;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {

    private final String message;
    private final LocalDateTime date;
    private boolean read;
    private final Viewer recipient;

    public Notification(String message, Viewer recipient) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        if (recipient == null) {
            throw new IllegalArgumentException("Recipient cannot be null.");
        }

        this.message = message;
        this.recipient = recipient;
        this.date = LocalDateTime.now();
        this.read = false;
    }

    public void markAsRead() {
        read = true;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isRead() {
        return read;
    }

    public Viewer getRecipient() {
        return recipient;
    }
}