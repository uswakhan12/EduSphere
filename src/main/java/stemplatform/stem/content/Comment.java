package stemplatform.stem.content;

import stemplatform.stem.users.User;

import java.time.LocalDateTime;

public class Comment {

    private final String commentId;
    private final User author;
    private String text;
    private final LocalDateTime date;

    public Comment(String commentId, User author, String text) {
        if (commentId == null || commentId.isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be empty.");
        }

        if (author == null) {
            throw new IllegalArgumentException("Comment author cannot be null.");
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        this.commentId = commentId;
        this.author = author;
        this.text = text;
        this.date = LocalDateTime.now();
    }

    public void edit(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        this.text = text;
    }

    public String getCommentId() {
        return commentId;
    }

    public User getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDate() {
        return date;
    }
}