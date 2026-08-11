package stemplatform.stem.users;

import stemplatform.stem.content.Comment;
import stemplatform.stem.content.Content;
import stemplatform.stem.management.ContentManager;

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

    public void removeContent(ContentManager contentManager, Content content) {
        if (contentManager == null) {
            throw new IllegalArgumentException("Content manager cannot be null.");
        }

        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        contentManager.removeContent(content);

        System.out.println("Content removed: " + content.getTitle());
    }

    public void removeComment(Content content, Comment comment) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null.");
        }

        content.removeComment(comment);

        System.out.println("Comment removed.");
    }
}