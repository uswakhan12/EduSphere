package stemplatform.stem.users;

import stemplatform.stem.content.Content;
import stemplatform.stem.content.Comment;
import stemplatform.stem.events.LiveEvent;
import stemplatform.stem.library.Library;
import stemplatform.stem.notifications.Notification;
import stemplatform.stem.streaming.StreamingSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Viewer extends User {

    private final Library library;
    private final List<Notification> notifications;
    private final Set<Creator> subscriptions;
    private final Set<LiveEvent> attendedEvents;

    public Viewer(
            String userId,
            String name,
            String email,
            String password) {

        super(userId, name, email, password);

        this.library = new Library();
        this.notifications = new ArrayList<>();
        this.subscriptions = new HashSet<>();
        this.attendedEvents = new HashSet<>();
    }

    public void subscribe(Creator creator) {
        if (creator == null) {
            throw new IllegalArgumentException("Creator cannot be null.");
        }

        if (subscriptions.add(creator)) {
            creator.addSubscriber(this);
        }
    }

    public void unsubscribe(Creator creator) {
        if (creator == null) {
            throw new IllegalArgumentException("Creator cannot be null.");
        }

        if (subscriptions.remove(creator)) {
            creator.removeSubscriber(this);
        }
    }

    public void like(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        content.addLike(this);
    }

    public void unlike(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        content.removeLike(this);
    }

    public void comment(String commentID, Content content, String text) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        Comment comment = new Comment(commentID, this, text);
        content.addComment(comment);
    }

    public void addFavorite(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        library.addFavorite(content);
    }

    public void removeFavorite(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        library.removeFavorite(content);
    }

    public void addToWatchLater(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        library.addToWatchLater(content);
    }

    public void removeFromWatchLater(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        library.removeFromWatchLater(content);
    }

    public Library getLibrary() {
        return library;
    }

    public void receiveNotification(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null.");
        }

        notifications.add(notification);
    }

    public List<Notification> getNotifications() {
        return List.copyOf(notifications);
    }

    public Set<Creator> getSubscriptions() {
        return Set.copyOf(subscriptions);
    }


    public Set<LiveEvent> getAttendedEvents() {
        return Set.copyOf(attendedEvents);
    }
}