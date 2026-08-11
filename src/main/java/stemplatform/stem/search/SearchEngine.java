package stemplatform.stem.search;

import stemplatform.stem.content.Content;

import java.util.ArrayList;
import java.util.List;

public class SearchEngine {

    private final List<Content> content;

    public SearchEngine(List<Content> content) {
        if (content == null) {
            throw new IllegalArgumentException("Content list cannot be null.");
        }

        this.content = content;
    }

    public Content[] search(String query) {
        if (query == null || query.isBlank()) {
            return new Content[0];
        }

        String searchQuery = query.toLowerCase().trim();
        List<Content> results = new ArrayList<>();

        for (Content item : content) {
            if (item.getTitle().toLowerCase().contains(searchQuery)
                    || item.getDescription().toLowerCase().contains(searchQuery)
                    || item.getSubject().toLowerCase().contains(searchQuery)) {

                results.add(item);
            }
        }

        return results.toArray(new Content[0]);
    }

    public Content[] filterBySubject(String subject) {
        if (subject == null || subject.isBlank()) {
            return new Content[0];
        }

        List<Content> results = new ArrayList<>();

        for (Content item : content) {
            if (item.getSubject().equalsIgnoreCase(subject.trim())) {
                results.add(item);
            }
        }

        return results.toArray(new Content[0]);
    }

    public Content[] filterByType(String type) {
        if (type == null || type.isBlank()) {
            return new Content[0];
        }

        String searchType = type.toLowerCase().trim();
        List<Content> results = new ArrayList<>();

        for (Content item : content) {
            if (item.getClass().getSimpleName().toLowerCase()
                    .equals(searchType)) {

                results.add(item);
            }
        }

        return results.toArray(new Content[0]);
    }
}