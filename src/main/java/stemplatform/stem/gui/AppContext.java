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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppContext {

    private final Stage primaryStage;
    private final FileManager fileManager;
    private final ApplicationState state;
    private final AuthenticationService authService;
    private final ContentManager contentManager;
    private final NotificationService notificationService;
    private final DownloadManager downloadManager;

    /*
     * Background auto-save: state is mutated continuously on the JavaFX
     * Application Thread as the user clicks around (publishing content,
     * liking, subscribing, etc). Rather than only persisting on a clean
     * shutdown -- which is lost if the app is force-closed or crashes --
     * a daemon thread wakes up periodically and writes the current state
     * to disk. Because this thread and the JavaFX thread can both touch
     * fileManager/state at the same time, every write path is funneled
     * through the synchronized save() method below so a periodic
     * auto-save can never interleave with a manual save and corrupt the
     * output file.
     */
    private final ScheduledExecutorService autoSaveExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "auto-save-thread");
                t.setDaemon(true);
                return t;
            });

    public AppContext(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.fileManager = new FileManager();
        this.state = fileManager.load();
        this.authService = new AuthenticationService(state.getUsers());
        this.contentManager = new ContentManager(state.getContent());
        this.notificationService = new NotificationService();
        this.downloadManager = new DownloadManager();

        autoSaveExecutor.scheduleAtFixedRate(
                this::save, 30, 30, TimeUnit.SECONDS);
    }

    public synchronized void save() {
        fileManager.save(state);
    }

    /**
     * Stops the background auto-save thread. Must be called on
     * application shutdown so the JVM can exit cleanly (a daemon
     * thread pool will not block exit on its own, but shutting it
     * down explicitly avoids a save firing after the final save()
     * in EduSphereApp.stop() has already run).
     */
    public void shutdownAutoSave() {
        autoSaveExecutor.shutdownNow();
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