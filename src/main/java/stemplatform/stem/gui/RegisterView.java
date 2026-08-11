package stemplatform.stem.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import stemplatform.stem.users.User;

public class RegisterView {

    public static Parent build(AppContext ctx) {
        VBox brandPane = new VBox(18);
        brandPane.getStyleClass().add("brand-pane");
        brandPane.setAlignment(Pos.CENTER_LEFT);
        brandPane.setPadding(new Insets(60));
        Label logo = new Label("EduSphere");
        logo.getStyleClass().add("brand-logo");
        Label tagline = new Label("Join as a Creator to publish STEM content,\nor as a Viewer to learn, watch and engage.");
        tagline.getStyleClass().add("brand-tagline");
        tagline.setWrapText(true);
        brandPane.getChildren().addAll(logo, tagline);

        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("auth-card");
        formCard.setMaxWidth(400);

        Label heading = new Label("Create your account");
        heading.getStyleClass().add("page-title");
        Label sub = new Label("Choose a role to get started");
        sub.getStyleClass().add("muted-text");

        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton viewerRadio = new RadioButton("Viewer");
        RadioButton creatorRadio = new RadioButton("Creator");
        viewerRadio.setToggleGroup(roleGroup);
        creatorRadio.setToggleGroup(roleGroup);
        viewerRadio.setSelected(true);
        viewerRadio.getStyleClass().add("role-radio");
        creatorRadio.getStyleClass().add("role-radio");
        HBox roleBox = new HBox(20, viewerRadio, creatorRadio);

        TextField nameField = UiKit.field("Full name");
        TextField emailField = UiKit.field("Email address");
        PasswordField passwordField = UiKit.passwordField("Password (min 6 characters)");
        PasswordField confirmField = UiKit.passwordField("Confirm password");
        TextArea bioArea = UiKit.textArea("Short bio (visible to your subscribers)", 3);
        bioArea.managedProperty().bind(bioArea.visibleProperty());
        bioArea.setVisible(false);

        creatorRadio.selectedProperty().addListener((obs, was, isNow) -> bioArea.setVisible(isNow));

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        Button createBtn = UiKit.primaryButton("Create Account");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            try {
                if (!passwordField.getText().equals(confirmField.getText())) {
                    throw new IllegalArgumentException("Passwords do not match.");
                }
                User newUser;
                if (creatorRadio.isSelected()) {
                    newUser = ctx.getAuthService().registerCreator(
                            nameField.getText().trim(),
                            emailField.getText().trim(),
                            passwordField.getText(),
                            bioArea.getText());
                } else {
                    newUser = ctx.getAuthService().registerViewer(
                            nameField.getText().trim(),
                            emailField.getText().trim(),
                            passwordField.getText());
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Account created for " + newUser.getName() + ". You can now log in.");
                alert.setHeaderText("Welcome to EduSphere");
                alert.showAndWait();
                ctx.setRoot(LoginView.build(ctx));
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        Hyperlink backLink = new Hyperlink("Already have an account? Log in");
        backLink.getStyleClass().add("link");
        backLink.setOnAction(e -> ctx.setRoot(LoginView.build(ctx)));

        formCard.getChildren().addAll(heading, sub, roleBox, nameField, emailField,
                passwordField, confirmField, bioArea, errorLabel, createBtn, backLink);

        VBox rightWrapper = new VBox(formCard);
        rightWrapper.setAlignment(Pos.CENTER);
        rightWrapper.getStyleClass().add("auth-right");
        HBox.setHgrow(rightWrapper, Priority.ALWAYS);

        HBox root = new HBox(brandPane, rightWrapper);
        root.getStyleClass().add("auth-root");
        return root;
    }
}