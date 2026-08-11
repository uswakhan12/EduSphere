package stemplatform.stem.management;

import stemplatform.stem.content.Downloadable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DownloadManager {

    /*
     * A small fixed thread pool handles file copies off the calling
     * thread (the JavaFX Application Thread, in this project) so a
     * large download does not freeze the UI. daemon = true so these
     * worker threads never prevent the JVM from exiting.
     */
    private final ExecutorService downloadExecutor =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "download-worker");
                t.setDaemon(true);
                return t;
            });

    /*
     * downloadHistory is shared mutable state: every worker thread in
     * the pool appends to the same list as downloads complete, and the
     * UI thread may read it at any time (e.g. to show a "recent
     * downloads" panel). All access is synchronized on this object's
     * monitor to prevent two threads from corrupting the list via a
     * concurrent structural modification.
     */
    private final List<String> downloadHistory = new ArrayList<>();

    /**
     * Synchronous download (blocking). Kept for callers -- such as
     * unit tests -- that want a simple call/return without dealing
     * with threads.
     */
    public void download(Downloadable content, String destinationPath)
            throws IOException {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Downloadable content cannot be null."
            );
        }

        Path source = Path.of(content.getFilePath());
        Path destination = Path.of(destinationPath);

        if (!Files.exists(source)) {
            throw new IOException(
                    "Source file does not exist: " + source
            );
        }

        Path parentDirectory = destination.getParent();

        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        Files.copy(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        synchronized (downloadHistory) {
            downloadHistory.add(destination.toString());
        }

        System.out.println(
                "File downloaded to: " + destination
        );
    }

    /**
     * Asynchronous download. The actual file copy runs on a background
     * thread from downloadExecutor. onSuccess/onFailure are invoked on
     * whatever thread calls them back with -- callers running inside
     * JavaFX should wrap their callback body in Platform.runLater(...)
     * since JavaFX scene-graph nodes may only be touched on the
     * Application Thread.
     */
    public void downloadAsync(
            Downloadable content,
            String destinationPath,
            Consumer<Path> onSuccess,
            Consumer<Exception> onFailure) {

        downloadExecutor.submit(() -> {
            try {
                download(content, destinationPath);
                if (onSuccess != null) {
                    onSuccess.accept(Path.of(destinationPath));
                }
            } catch (Exception e) {
                if (onFailure != null) {
                    onFailure.accept(e);
                }
            }
        });
    }

    public List<String> getDownloadHistory() {
        synchronized (downloadHistory) {
            return List.copyOf(downloadHistory);
        }
    }

    public void shutdown() {
        downloadExecutor.shutdownNow();
    }
}