package stemplatform.stem.library;

import stemplatform.stem.content.Content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Library implements Serializable {

    private final Set<Content> favorites;
    private final Set<Content> watchLater;
    private final List<Content> history;

    public Library() {
        favorites = new HashSet<>();
        watchLater = new HashSet<>();
        history = new ArrayList<>();
    }

    public void addFavorite(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        favorites.add(content);
    }

    public void removeFavorite(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        favorites.remove(content);
    }

    public void addToWatchLater(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        watchLater.add(content);
    }

    public void removeFromWatchLater(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        watchLater.remove(content);
    }

    public void addToHistory(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        history.remove(content);
        history.add(content);
    }

    public Set<Content> getFavorites() {
        return Set.copyOf(favorites);
    }

    public Set<Content> getWatchLater() {
        return Set.copyOf(watchLater);
    }

    public List<Content> getHistory() {
        return List.copyOf(history);
    }
}
