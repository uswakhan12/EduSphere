package stemplatform.stem.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import stemplatform.stem.users.Administrator;
import stemplatform.stem.users.Creator;
import stemplatform.stem.users.User;
import stemplatform.stem.users.Viewer;

public class LoginView {

    public static Parent build(AppContext ctx) {
        VBox brandPane = new VBox(18);
        brandPane.getStyleClass().add("brand-pane");
        brandPane.setAlignment(Pos.CENTER_LEFT);
        brandPane.setPadding(new Insets(60));

        Label logo = new Label("EduSphere");
        logo.getStyleClass().add("brand-logo");

        Label tagline = new Label("Learn. Create. Share.\n\nA unified STEM content platform for Admins, Creators and Viewers.");
        tagline.getStyleClass().add("brand-tagline");
        tagline.setWrapText(true);

        brandPane.getChildren().addAll(logo, tagline);

        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("auth-card");
        formCard.setMaxWidth(380);

        Label heading = new Label("Welcome back");
        heading.getStyleClass().add("page-title");
        Label sub = new Label("Sign in to continue to your dashboard");
        sub.getStyleClass().add("muted-text");

        TextField emailField = UiKit.field("Email address");
        PasswordField passwordField = UiKit.passwordField("Password");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        Button loginBtn = UiKit.primaryButton("Log In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            try {
                User user = ctx.getAuthService().login(
                        emailField.getText().trim(), passwordField.getText());
                routeToDashboard(ctx, user);
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        Hyperlink registerLink = new Hyperlink("Don't have an account? Create one");
        registerLink.getStyleClass().add("link");
        registerLink.setOnAction(e -> ctx.setRoot(RegisterView.build(ctx)));

        Label hint = new Label("Default admin login: admin@edusphere.com / admin123");
        hint.getStyleClass().add("hint-text");

        formCard.getChildren().addAll(heading, sub, emailField, passwordField,
                errorLabel, loginBtn, registerLink, hint);

        VBox rightWrapper = new VBox(formCard);
        rightWrapper.setAlignment(Pos.CENTER);
        rightWrapper.getStyleClass().add("auth-right");
        HBox.setHgrow(rightWrapper, Priority.ALWAYS);

        HBox root = new HBox(brandPane, rightWrapper);
        root.getStyleClass().add("auth-root");
        return root;
    }

    static void routeToDashboard(AppContext ctx, User user) {
        if (user instanceof Administrator admin) {
            ctx.setRoot(AdminDashboardView.build(ctx, admin));
        } else if (user instanceof Creator creator) {
            ctx.setRoot(CreatorDashboardView.build(ctx, creator));
        } else if (user instanceof Viewer viewer) {
            ctx.setRoot(ViewerDashboardView.build(ctx, viewer));
        }
    }
}