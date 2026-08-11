package stemplatform.stem.content;

import stemplatform.stem.users.Creator;

public class Presentation extends Content implements Downloadable {

    private final String filePath;
    private final int slideCount;
    private final String presentationType;

    public Presentation(
            String contentId,
            Creator creator,
            String title,
            String description,
            String subject,
            String filePath,
            int slideCount,
            String presentationType) {

        super(contentId, creator, title, description, subject);

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        if (slideCount <= 0) {
            throw new IllegalArgumentException("Slide count must be positive.");
        }

        this.filePath = filePath;
        this.slideCount = slideCount;
        this.presentationType = presentationType;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    public int getSlideCount() {
        return slideCount;
    }

    public String getPresentationType() {
        return presentationType;
    }
}