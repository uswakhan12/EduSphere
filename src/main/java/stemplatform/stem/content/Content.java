package stemplatform.stem.content;

import stemplatform.stem.users.Creator;
import stemplatform.stem.users.User;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Content implements Serializable {

    private final String contentId;
    private final Creator creator;
    private String title;
    private String description;

    private int viewCount;
    private String subject;
    private final LocalDate uploadDate;

    private final List<Comment> comments;
    private final Set<User> likes;

    public Content(
            String contentId,
            Creator creator,
            String title,
            String description,
            String subject) {

        this.contentId = contentId;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.uploadDate = LocalDate.now();

        this.comments = new ArrayList<>();
        this.likes = new HashSet<>();
    }

    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null.");
        }

        comments.add(comment);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
    }

    public void addLike(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        likes.add(user);
    }

    public void removeLike(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        likes.remove(user);
    }

    public int getLikeCount() {
        return likes.size();
    }

    public boolean isLikedBy(User user) {
        return likes.contains(user);
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

    public List<Comment> getComments() {
        return List.copyOf(comments);
    }

    public int getViewCount() {
        return viewCount;
    }

    public void incrementViewCount(){
        viewCount++;
    }
}