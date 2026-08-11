package stemplatform.stem.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import stemplatform.stem.users.Administrator;

public class EduSphereApp extends Application {

    private AppContext context;

    @Override
    public void start(Stage primaryStage) {
        context = new AppContext(primaryStage);
        seedDefaultAdminIfNeeded();

        primaryStage.setTitle("EduSphere - STEM Learning Platform");
        primaryStage.setMinWidth(1150);
        primaryStage.setMinHeight(740);

        Scene scene = new Scene(LoginView.build(context), 1280, 800);
        scene.getStylesheets().add(
                getClass().getResource("/stemplatform/stem/css/edusphere.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void seedDefaultAdminIfNeeded() {
        boolean hasAdmin = context.getState().getUsers().stream()
                .anyMatch(u -> u instanceof Administrator);
        if (!hasAdmin) {
            context.getAuthService().registerAdministrator(
                    "System Admin", "admin@edusphere.com", "admin123");
        }
    }

    @Override
    public void stop() {
        context.shutdownAutoSave();
        context.save();
    }

    public static void main(String[] args) {
        launch(args);
    }
}