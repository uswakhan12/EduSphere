package stemplatform.stem.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class UiKit {

    public static TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("input-field");
        return tf;
    }

    public static PasswordField passwordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.getStyleClass().add("input-field");
        return pf;
    }

    public static TextArea textArea(String prompt, int rows) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(rows);
        ta.setWrapText(true);
        ta.getStyleClass().add("input-field");
        return ta;
    }

    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-primary");
        return b;
    }

    public static Button secondaryButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-secondary");
        return b;
    }

    public static Button dangerButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-danger");
        return b;
    }

    public static Button ghostButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-ghost");
        return b;
    }

    public static Label badge(String text, String kind) {
        Label l = new Label(text);
        l.getStyleClass().addAll("badge", "badge-" + kind);
        return l;
    }

    public static VBox card(Node... children) {
        VBox box = new VBox(10, children);
        box.getStyleClass().add("card");
        return box;
    }

    public static VBox statTile(String label, String value, String accent) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");
        VBox box = new VBox(4, valueLabel, nameLabel);
        box.getStyleClass().addAll("stat-tile", "accent-" + accent);
        box.setPrefWidth(210);
        return box;
    }

    public static Button navButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-item");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }

    public static void setActiveNav(Button active, Button... all) {
        for (Button b : all) {
            b.getStyleClass().remove("nav-item-active");
        }
        if (!active.getStyleClass().contains("nav-item-active")) {
            active.getStyleClass().add("nav-item-active");
        }
    }

    public static HBox topBar(String title, String userName, String roleLabel, Runnable onLogout) {
        Label pageTitle = new Label(title);
        pageTitle.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label who = new Label(userName + "  ");
        who.getStyleClass().add("topbar-user");
        Label role = badge(roleLabel, "info");

        Button logoutBtn = ghostButton("Log Out");
        logoutBtn.setOnAction(e -> onLogout.run());

        HBox bar = new HBox(12, pageTitle, spacer, who, role, logoutBtn);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }
}