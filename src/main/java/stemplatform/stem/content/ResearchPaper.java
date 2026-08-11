package stemplatform.stem.content;


import stemplatform.stem.users.Creator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;

public class ResearchPaper extends Content implements Downloadable {

    private final String filePath;
    private final String[] authors;
    private final String abstractText;
    private final String publication;
    private final LocalDate publicationDate;

    public ResearchPaper(
            String contentId,
            Creator creator,
            String title,
            String description,
            String subject,
            String filePath,
            String[] authors,
            String abstractText,
            String publication,
            LocalDate publicationDate) {

        super(contentId, creator, title, description, subject);

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        if (authors == null || authors.length == 0) {
            throw new IllegalArgumentException("A research paper must have at least one author.");
        }

        if (abstractText == null || abstractText.isBlank()) {
            throw new IllegalArgumentException("Abstract cannot be empty.");
        }

        this.filePath = filePath;
        this.authors = Arrays.copyOf(authors, authors.length);
        this.abstractText = abstractText;
        this.publication = publication;
        this.publicationDate = publicationDate;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    public String[] getAuthors() {
        return Arrays.copyOf(authors, authors.length);
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String getPublication() {
        return publication;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

}