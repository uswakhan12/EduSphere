package stemplatform.stem.streaming;

public interface Streamable {

    void start();

    void pause();

    void resume();

    void stop();

    void seek(int position);
}