package stemplatform.stem.content;

import stemplatform.stem.users.Creator;

import java.io.Serializable;

public class Video extends Content implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String filePath;
    private final int duration;

    public Video(
            String contentId,
            Creator creator,
            String title,
            String description,
            String subject,
            String filePath,
            int duration) {

        super(
                contentId,
                creator,
                title,
                description,
                subject
        );

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "File path cannot be empty."
            );
        }

        if (duration <= 0) {
            throw new IllegalArgumentException(
                    "Video duration must be greater than zero."
            );
        }

        this.filePath = filePath;
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getDuration() {
        return duration;
    }
}
