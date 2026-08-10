package stemplatform.stem.users;

import java.io.Serializable;

public abstract class User implements Serializable {

    private final String userId;
    private String name;
    private String email;
    private String password;
    private boolean banned;

    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.banned = false;
    }

    public boolean login(String email, String password) {
        return !banned
                && this.email.equals(email)
                && this.password.equals(password);
    }

    public void logout() {
        // Logout behavior can be handled by the application/session manager.
    }

    public void ban() {
        banned = true;
    }

    public void unban() {
        banned = false;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isBanned() {
        return banned;
    }
}