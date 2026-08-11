package stemplatform.stem.users;

import stemplatform.stem.content.Comment;
import stemplatform.stem.content.Content;
import stemplatform.stem.content.Video;
import stemplatform.stem.events.LiveEvent;
import stemplatform.stem.library.Library;
import stemplatform.stem.notifications.Notification;
import stemplatform.stem.streaming.StreamingSession;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Viewer extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Library library;
    private Set<Notification> notifications;
    private Set<Creator> subscriptions;
    private Set<LiveEvent> attendedEvents;

    public Viewer(
            String userId,
            String name,
            String email,
            String password
    ) {
        super(userId, name, email, password);

        this.library = new Library();
        this.notifications = new HashSet<>();
        this.subscriptions = new HashSet<>();
        this.attendedEvents = new HashSet<>();
    }

    public void subscribe(Creator creator) {

        if (creator == null) {
            throw new IllegalArgumentException(
                    "Creator cannot be null."
            );
        }

        if (subscriptions.add(creator)) {
            creator.addSubscriber(this);
        }
    }

    public void unsubscribe(Creator creator) {

        if (creator == null) {
            throw new IllegalArgumentException(
                    "Creator cannot be null."
            );
        }

        if (subscriptions.remove(creator)) {
            creator.removeSubscriber(this);
        }
    }

    public void like(Content content) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Content cannot be null."
            );
        }

        content.addLike(this);
    }

    public void unlike(Content content) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Content cannot be null."
            );
        }

        content.removeLike(this);
    }


    public void comment(Content content, String text) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Content cannot be null."
            );
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Comment cannot be empty."
            );
        }

        Comment comment = new Comment(
                generateCommentId(),
                this,
                text
        );

        content.addComment(comment);
    }

    public void addFavorite(Content content) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Content cannot be null."
            );
        }

        library.addFavorite(content);
    }

    public void removeFavorite(Content content) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Content cannot be null."
            );
        }

        library.removeFavorite(content);
    }


    public void addToWatchLater(Content content) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Content cannot be null."
            );
        }

        library.addToWatchLater(content);
    }

    public void removeFromWatchLater(Content content) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Content cannot be null."
            );
        }

        library.removeFromWatchLater(content);
    }


    public StreamingSession watch(Video video) {

        if (video == null) {
            throw new IllegalArgumentException(
                    "Video cannot be null."
            );
        }

        StreamingSession session = new StreamingSession(this, video);

        // Starting the session records the view and adds the video
        // to this viewer's history (handled inside StreamingSession).
        session.start();

        return session;
    }

    public void receiveNotification(Notification notification) {

        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification cannot be null."
            );
        }

        notifications.add(notification);
    }


    public void attendEvent(LiveEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Live event cannot be null."
            );
        }

        if (event.join(this)) {
            attendedEvents.add(event);
        }
    }

    public void leaveEvent(LiveEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Live event cannot be null."
            );
        }

        event.leave(this);
        attendedEvents.remove(event);
    }

    public Library getLibrary() {
        return library;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public Set<Creator> getSubscriptions() {
        return subscriptions;
    }

    public Set<LiveEvent> getAttendedEvents() {
        return attendedEvents;
    }

    private String generateCommentId() {
        return java.util.UUID.randomUUID().toString();
    }
}
