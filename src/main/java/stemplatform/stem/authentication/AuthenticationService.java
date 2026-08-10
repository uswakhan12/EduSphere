package stemplatform.stem.authentication;

import stemplatform.stem.users.Administrator;
import stemplatform.stem.users.Creator;
import stemplatform.stem.users.User;
import stemplatform.stem.users.Viewer;

import java.util.List;
import java.util.UUID;

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

    public User registerViewer(
            String name,
            String email,
            String password
    ) {
        validateRegistrationData(name, email, password);

        ensureEmailAvailable(email);

        String userId = generateUserId();

        Viewer viewer = new Viewer(
                userId,
                name,
                email,
                password
        );

        users.add(viewer);

        return viewer;
    }

    public User registerCreator(
            String name,
            String email,
            String password,
            String bio
    ) {
        validateRegistrationData(name, email, password);

        if (bio == null) {
            throw new IllegalArgumentException(
                    "Bio cannot be null."
            );
        }

        ensureEmailAvailable(email);

        String userId = generateUserId();

        Creator creator = new Creator(
                userId,
                name,
                email,
                password,
                bio
        );

        users.add(creator);

        return creator;
    }

    public Administrator registerAdministrator(
            String name,
            String email,
            String password
    ) {
        validateRegistrationData(name, email, password);

        ensureEmailAvailable(email);

        String userId = generateUserId();

        Administrator administrator = new Administrator(
                userId,
                name,
                email,
                password
        );

        users.add(administrator);

        return administrator;
    }

    public User login(String email, String password) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        if (currentUser != null) {
            throw new IllegalStateException(
                    "A user is already logged in."
            );
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

                user.login(email, password);

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

    public User findUserByEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        for (User user : users) {

            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }

        return null;
    }

    public User findUserById(String userId) {

        if (userId == null || userId.isBlank()) {
            return null;
        }

        for (User user : users) {

            if (user.getUserId().equals(userId)) {
                return user;
            }
        }

        return null;
    }

    private void validateRegistrationData(
            String name,
            String email,
            String password
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name cannot be empty."
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty."
            );
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Invalid email format."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters."
            );
        }
    }

    private void ensureEmailAvailable(String email) {

        if (findUserByEmail(email) != null) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }
    }

    private boolean isValidEmail(String email) {

        return email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
    }


    private String generateUserId() {
        return UUID.randomUUID().toString();
    }
}