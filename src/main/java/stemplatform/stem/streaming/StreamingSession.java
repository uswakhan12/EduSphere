package stemplatform.stem.streaming;

import stemplatform.stem.content.Video;
import stemplatform.stem.users.Viewer;

public class StreamingSession implements Streamable {

    private int currentPosition;
    private String status;

    private final Viewer viewer;
    private final Video video;
    private boolean viewRecorded;

    public StreamingSession(Viewer viewer, Video video) {

        if (viewer == null) {
            throw new IllegalArgumentException(
                    "Viewer cannot be null."
            );
        }

        if (video == null) {
            throw new IllegalArgumentException(
                    "Video cannot be null."
            );
        }

        this.viewer = viewer;
        this.video = video;
        this.currentPosition = 0;
        this.status = "STOPPED";
        this.viewRecorded = false;
    }


    @Override
    public void play() {

        if (status.equals("PLAYING")) {
            return;
        }
        if (currentPosition >= video.getDuration()) {
            currentPosition = 0;
        }

        status = "PLAYING";

        if (!viewRecorded) {
            video.incrementViewCount();
            viewer.getLibrary().addToHistory(video);

            viewRecorded = true;
        }
    }

    @Override
    public void pause() {

        if (!status.equals("PLAYING")) {
            return;
        }

        status = "PAUSED";
    }

    @Override
    public void resume() {

        if (!status.equals("PAUSED")) {
            return;
        }

        status = "PLAYING";
    }

    @Override
    public void stop() {

        status = "STOPPED";
        currentPosition = 0;
    }
    @Override
    public void seek(int position) {

        if (position < 0 || position > video.getDuration()) {
            throw new IllegalArgumentException(
                    "Position must be between 0 and video duration."
            );
        }

        currentPosition = position;
    }

    public void updatePosition(int position) {

        if (position < 0 || position > video.getDuration()) {
            throw new IllegalArgumentException(
                    "Position must be between 0 and video duration."
            );
        }

        currentPosition = position;

        if (currentPosition == video.getDuration()) {
            status = "STOPPED";
        }
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

    public boolean isPlaying() {
        return status.equals("PLAYING");
    }

    public boolean isPaused() {
        return status.equals("PAUSED");
    }

    public boolean isStopped() {
        return status.equals("STOPPED");
    }

    public boolean isViewRecorded() {
        return viewRecorded;
    }
}
