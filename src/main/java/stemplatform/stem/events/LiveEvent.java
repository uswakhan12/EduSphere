package stemplatform.stem.events;

import stemplatform.stem.users.Creator;
import stemplatform.stem.users.Viewer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LiveEvent implements Serializable{

    private final String eventId;
    private final String title;
    private final LocalDateTime startTime;
    private final int maximumViewers;

    private transient Set<Viewer> currentViewers;
    private final Creator host;

    public LiveEvent(
            String eventId,
            String title,
            LocalDateTime startTime,
            int maximumViewers,
            Creator host) {

        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be empty.");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Event title cannot be empty.");
        }

        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null.");
        }

        if (maximumViewers <= 0) {
            throw new IllegalArgumentException(
                    "Maximum viewers must be greater than zero."
            );
        }

        if (host == null) {
            throw new IllegalArgumentException("Host cannot be null.");
        }

        this.eventId = eventId;
        this.title = title;
        this.startTime = startTime;
        this.maximumViewers = maximumViewers;
        this.host = host;

        this.currentViewers = new HashSet<>();
    }

    /*
     * This method is synchronized because checking the current
     * number of viewers and adding a new viewer must happen
     * atomically.
     */
    public synchronized boolean join(Viewer viewer) {

        if (viewer == null) {
            throw new IllegalArgumentException("Viewer cannot be null.");
        }

        // Viewer is already attending.
        if (currentViewers.contains(viewer)) {
            return false;
        }

        // Event is already full.
        if (currentViewers.size() >= maximumViewers) {
            return false;
        }

        currentViewers.add(viewer);

        return true;
    }

    public synchronized void leave(Viewer viewer) {

        if (viewer == null) {
            throw new IllegalArgumentException("Viewer cannot be null.");
        }

        currentViewers.remove(viewer);
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getMaximumViewers() {
        return maximumViewers;
    }

    public Creator getHost() {
        return host;
    }

    public synchronized Set<Viewer> getCurrentViewers() {
        return Collections.unmodifiableSet(
                new HashSet<>(currentViewers)
        );
    }

    public synchronized int getCurrentViewerCount() {
        return currentViewers.size();
    }

    /*
     * currentViewers is transient because it is live runtime state,
     * not something that should be persisted. Since transient fields
     * come back as null after deserialization, we reinitialize it
     * here so a reloaded event starts with nobody attending instead
     * of throwing a NullPointerException the first time join()/leave()
     * is called.
     */
    private void readObject(ObjectInputStream input)
            throws IOException, ClassNotFoundException {

        input.defaultReadObject();
        currentViewers = new HashSet<>();
    }
}