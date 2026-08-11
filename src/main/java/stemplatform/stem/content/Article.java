package stemplatform.stem.content;

import stemplatform.stem.users.Creator;

public class Article extends Content {

    private String body;
    private int readingTime;

    public Article(
            String contentId,
            Creator creator,
            String title,
            String description,
            String subject,
            String body,
            int readingTime) {

        super(contentId, creator, title, description, subject);

        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Article body cannot be empty.");
        }

        if (readingTime <= 0) {
            throw new IllegalArgumentException("Reading time must be positive.");
        }

        this.body = body;
        this.readingTime = readingTime;
    }

    public String getBody() {
        return body;
    }

    public int getReadingTime() {
        return readingTime;
    }

    public void updateBody(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Article body cannot be empty.");
        }

        this.body = body;
    }
}