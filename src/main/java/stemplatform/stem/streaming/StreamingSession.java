package stemplatform.stem.streaming;

import stemplatform.stem.content.Video;
import stemplatform.stem.users.Viewer;

import java.io.Serializable;

public class StreamingSession  {

    private int currentPosition;
    private String status;

    private final Viewer viewer;
    private final Video video;

    public StreamingSession(Viewer viewer, Video video) {
        if (viewer == null) {
            throw new IllegalArgumentException("Viewer cannot be null.");
        }

        if (video == null) {
            throw new IllegalArgumentException("Video cannot be null.");
        }

        this.viewer = viewer;
        this.video = video;
        this.currentPosition = 0;
        this.status = "STOPPED";
    }

    public void start() {
        if (status.equals("PLAYING")) {
            return;
        }

        status = "PLAYING";
    }

    public void pause() {
        if (!status.equals("PLAYING")) {
            return;
        }

        status = "PAUSED";
    }

    public void resume() {
        if (!status.equals("PAUSED")) {
            return;
        }

        status = "PLAYING";
    }

    public void stop() {
        status = "STOPPED";
        currentPosition = 0;
    }

    public void seek(int position) {
        if (position < 0 || position > video.getDuration()) {
            throw new IllegalArgumentException(
                    "Position must be between 0 and the video duration."
            );
        }

        currentPosition = position;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public String getStatus() {
        return status;
    }

    public Viewer getViewer() {
        return viewer;
    }

    public Video getVideo() {
        return video;
    }
}