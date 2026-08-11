package stemplatform.stem.users;

import stemplatform.stem.content.Content;
import stemplatform.stem.events.LiveEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Creator extends User {

    private String bio;

    private final Set<Viewer> subscribers;
    private final List<Content> publishedContent;
    private final List<LiveEvent> liveEvents;

    public Creator(
            String userId,
            String name,
            String email,
            String password,
            String bio) {

        super(userId, name, email, password);

        this.bio = bio;
        this.subscribers = new HashSet<>();
        this.publishedContent = new ArrayList<>();
        this.liveEvents = new ArrayList<>();
    }

    public void addPublishedContent(Content content) {

        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        if (!publishedContent.contains(content)) {
            publishedContent.add(content);
        }
    }

    public void removeContent(Content content) {

        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        publishedContent.remove(content);
    }

    public void createLiveEvent(LiveEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (event.getHost() != this) {
            throw new IllegalArgumentException(
                    "This creator is not the host of the event."
            );
        }

        if (!liveEvents.contains(event)) {
            liveEvents.add(event);
        }
    }

    public void addSubscriber(Viewer viewer) {

        if (viewer == null) {
            throw new IllegalArgumentException("Viewer cannot be null.");
        }

        subscribers.add(viewer);
    }

    public void removeSubscriber(Viewer viewer) {

        if (viewer == null) {
            throw new IllegalArgumentException("Viewer cannot be null.");
        }

        subscribers.remove(viewer);
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<Viewer> getSubscribers() {
        return Set.copyOf(subscribers);
    }

    public List<Content> getPublishedContent() {
        return List.copyOf(publishedContent);
    }

    public List<LiveEvent> getLiveEvents() {
        return List.copyOf(liveEvents);
    }
}