package stemplatform.stem.management;

import stemplatform.stem.content.Downloadable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DownloadManager {

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

        System.out.println(
                "File downloaded to: " + destination
        );
    }
}