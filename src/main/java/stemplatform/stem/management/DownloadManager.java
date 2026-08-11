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

    private final ExecutorService downloadExecutor =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "download-worker");
                t.setDaemon(true);
                return t;
            });

    private final List<String> downloadHistory = new ArrayList<>();

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