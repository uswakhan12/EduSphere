package stemplatform.stem.content;

import stemplatform.stem.users.Creator;
import stemplatform.stem.users.User;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class Content implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String contentId;
    private final Creator creator;

    private String title;
    private String description;
    private String subject;

    private int viewCount;
    private final LocalDate uploadDate;

    private final List<Comment> comments;
    private final Set<User> likes;

    public Content(
            String contentId,
            Creator creator,
            String title,
            String description,
            String subject) {

        if (contentId == null || contentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Content ID cannot be empty."
            );
        }

        if (creator == null) {
            throw new IllegalArgumentException(
                    "Creator cannot be null."
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Title cannot be empty."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty."
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Subject cannot be empty."
            );
        }

        this.contentId = contentId;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.subject = subject;

        this.viewCount = 0;
        this.uploadDate = LocalDate.now();

        this.comments = new ArrayList<>();
        this.likes = new HashSet<>();
    }

    public void addComment(Comment comment) {

        if (comment == null) {
            throw new IllegalArgumentException(
                    "Comment cannot be null."
            );
        }

        if (!comments.contains(comment)) {
            comments.add(comment);
        }
    }

    public void removeComment(Comment comment) {

        if (comment == null) {
            return;
        }

        comments.remove(comment);
    }

    // ============================================================
    // LIKES
    // ============================================================

    public void addLike(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null."
            );
        }

        likes.add(user);
    }

    public void removeLike(User user) {

        if (user == null) {
            return;
        }

        likes.remove(user);
    }

    public boolean isLikedBy(User user) {

        if (user == null) {
            return false;
        }

        return likes.contains(user);
    }

    public int getLikeCount() {
        return likes.size();
    }

    public synchronized void incrementViewCount() {
        viewCount++;
    }

    public int getViewCount() {
        return viewCount;
    }

    public String getContentId() {
        return contentId;
    }

    public Creator getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setTitle(String title) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Title cannot be empty."
            );
        }

        this.title = title;
    }

    public void setDescription(String description) {

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty."
            );
        }

        this.description = description;
    }

    public void setSubject(String subject) {

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Subject cannot be empty."
            );
        }

        this.subject = subject;
    }


    public List<Comment> getComments() {
        return List.copyOf(comments);
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Content other)) {
            return false;
        }

        return contentId.equals(other.contentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentId);
    }

    @Override
    public String toString() {
        return "Content{" +
                "contentId='" + contentId + '\'' +
                ", title='" + title + '\'' +
                ", subject='" + subject + '\'' +
                ", viewCount=" + viewCount +
                '}';
    }
}
