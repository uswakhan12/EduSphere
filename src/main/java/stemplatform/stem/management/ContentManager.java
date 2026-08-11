package stemplatform.stem.management;

import stemplatform.stem.content.Content;
import stemplatform.stem.users.Creator;
import java.util.List;

public class ContentManager {

    private final List<Content> allContent;

    public ContentManager(List<Content> allContent) {
        this.allContent = allContent;
    }

    public void publishContent(Creator creator, Content content) {
        if (creator == null) {
            throw new IllegalArgumentException("Creator cannot be null.");
        }

        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        if (content.getCreator() != creator) {
            throw new IllegalArgumentException(
                    "Content does not belong to the specified creator."
            );
        }

        if (allContent.contains(content)) {
            throw new IllegalArgumentException(
                    "Content has already been published."
            );
        }

        creator.addPublishedContent(content);
        allContent.add(content);
    }

    public void removeContent(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        if (!allContent.remove(content)) {
            return;
        }

        Creator creator = content.getCreator();

        if (creator != null) {
            creator.removeContent(content);
        }
    }

    public Content getContentById(String contentId) {
        if (contentId == null || contentId.isBlank()) {
            throw new IllegalArgumentException("Content ID cannot be empty.");
        }

        for (Content content : allContent) {
            if (content.getContentId().equals(contentId)) {
                return content;
            }
        }

        return null;
    }

    public List<Content> getAllContent() {
        return List.copyOf(allContent);
    }
}