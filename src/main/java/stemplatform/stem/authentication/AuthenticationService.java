package stemplatform.stem.authentication;

import stemplatform.stem.users.User;

import java.util.List;

public class AuthenticationService {

    private final List<User> users;
    private User currentUser;

    public AuthenticationService(List<User> users) {
        if (users == null) {
            throw new IllegalArgumentException("Users list cannot be null.");
        }

        this.users = users;
        this.currentUser = null;
    }

    public User login(String email, String password) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (currentUser != null) {
            throw new IllegalStateException("A user is already logged in.");
        }

        for (User user : users) {

            if (user.getEmail().equalsIgnoreCase(email)
                    && user.getPassword().equals(password)) {

                if (user.isBanned()) {
                    throw new IllegalStateException(
                            "This user has been banned."
                    );
                }

                currentUser = user;
                return user;
            }
        }

        throw new IllegalArgumentException(
                "Invalid email or password."
        );
    }

    public void logout() {

        if (currentUser == null) {
            throw new IllegalStateException(
                    "No user is currently logged in."
            );
        }

        currentUser.logout();
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}

