package stemplatform.stem.gui;

import javafx.scene.Parent;
import javafx.stage.Stage;
import stemplatform.stem.authentication.AuthenticationService;
import stemplatform.stem.management.ContentManager;
import stemplatform.stem.management.DownloadManager;
import stemplatform.stem.notifications.NotificationService;
import stemplatform.stem.storage.ApplicationState;
import stemplatform.stem.storage.FileManager;
import stemplatform.stem.users.User;

public class AppContext {

    private final Stage primaryStage;
    private final FileManager fileManager;
    private final ApplicationState state;
    private final AuthenticationService authService;
    private final ContentManager contentManager;
    private final NotificationService notificationService;
    private final DownloadManager downloadManager;

    public AppContext(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.fileManager = new FileManager();
        this.state = fileManager.load();
        this.authService = new AuthenticationService(state.getUsers());
        this.contentManager = new ContentManager(state.getContent());
        this.notificationService = new NotificationService();
        this.downloadManager = new DownloadManager();
    }

    public void save() {
        fileManager.save(state);
    }

    public void setRoot(Parent root) {
        primaryStage.getScene().setRoot(root);
    }

    public Stage getPrimaryStage() { return primaryStage; }
    public ApplicationState getState() { return state; }
    public AuthenticationService getAuthService() { return authService; }
    public ContentManager getContentManager() { return contentManager; }
    public NotificationService getNotificationService() { return notificationService; }
    public DownloadManager getDownloadManager() { return downloadManager; }
    public User getCurrentUser() { return authService.getCurrentUser(); }
}