package stemplatform.stem.streaming;

public interface Streamable {

    void play();

    void pause();

    void resume();

    void stop();

    void seek(int position);
}