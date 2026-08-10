package stemplatform.stem.users;

import stemplatform.stem.content.Comment;
import stemplatform.stem.content.Content;

public class Administrator extends User {

    public Administrator(
            String userId,
            String name,
            String email,
            String password) {

        super(userId, name, email, password);
    }

    public void banUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (user == this) {
            throw new IllegalArgumentException("Administrator cannot ban themselves.");
        }

        user.ban();
    }

    public void viewContent(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        System.out.println("Viewing content: " + content.getTitle());
    }

    public void removeContent(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        System.out.println("Content removed: " + content.getTitle());
    }

    public void removeComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null.");
        }

        System.out.println("Comment removed.");
    }
}