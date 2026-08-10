package stemplatform.stem.content;

import stemplatform.stem.streaming.Streamable;
import stemplatform.stem.users.Creator;

public class Video extends Content implements Streamable {

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
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        if (duration <= 0) {
            throw new IllegalArgumentException(
                    "Video duration must be greater than zero."
            );
        }

        this.filePath = filePath;
        this.duration = duration;
    }

    @Override
    public void play() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void seek(int position) {
        if (position < 0 || position > duration) {
            throw new IllegalArgumentException(
                    "Position must be between 0 and video duration."
            );
        }

    }

    public String getFilePath() {
        return filePath;
    }

    public int getDuration() {
        return duration;
    }
}