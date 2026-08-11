package stemplatform.stem.gui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import stemplatform.stem.content.Content;
import stemplatform.stem.events.LiveEvent;
import stemplatform.stem.users.Administrator;
import stemplatform.stem.users.Creator;
import stemplatform.stem.users.User;
import stemplatform.stem.users.Viewer;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardView {

    private static AppContext ctx;
    private static Administrator admin;
    private static StackPane content;
    private static Button navOverview, navUsers, navContent, navEvents;

    public static Parent build(AppContext appCtx, Administrator adminUser) {
        ctx = appCtx;
        admin = adminUser;

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-bg");

        navOverview = UiKit.navButton("Overview");
        navUsers = UiKit.navButton("Manage Users");
        navContent = UiKit.navButton("Manage Content");
        navEvents = UiKit.navButton("Live Events");

        Label brand = new Label("EduSphere");
        brand.getStyleClass().add("sidebar-brand");
        Label sub = new Label("Admin Console");
        sub.getStyleClass().add("sidebar-subtitle");
        Region gap = new Region();
        gap.setPrefHeight(18);

        VBox sidebar = new VBox(6, brand, sub, gap, navOverview, navUsers, navContent, navEvents);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(24, 14, 24, 14));
        root.setLeft(sidebar);

        HBox topBar = UiKit.topBar("Admin Dashboard", admin.getName(), "ADMINISTRATOR",
                () -> { ctx.getAuthService().logout(); ctx.setRoot(LoginView.build(ctx)); });

        content = new StackPane();
        content.setPadding(new Insets(24));
        VBox centerBox = new VBox(topBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.setCenter(centerBox);

        Button[] navs = { navOverview, navUsers, navContent, navEvents };
        navOverview.setOnAction(e -> { showOverview(); UiKit.setActiveNav(navOverview, navs); });
        navUsers.setOnAction(e -> { showUsers(); UiKit.setActiveNav(navUsers, navs); });
        navContent.setOnAction(e -> { showContent(); UiKit.setActiveNav(navContent, navs); });
        navEvents.setOnAction(e -> { showEvents(); UiKit.setActiveNav(navEvents, navs); });

        showOverview();
        UiKit.setActiveNav(navOverview, navs);
        return root;
    }

    private static void showOverview() {
        List<User> users = ctx.getState().getUsers();
        long admins = users.stream().filter(u -> u instanceof Administrator).count();
        long creators = users.stream().filter(u -> u instanceof Creator).count();
        long viewers = users.stream().filter(u -> u instanceof Viewer).count();
        long banned = users.stream().filter(User::isBanned).count();
        int totalContent = ctx.getContentManager().getAllContent().size();
        int totalEvents = ctx.getState().getEvents().size();

        FlowPane stats = new FlowPane(16, 16);
        stats.getChildren().addAll(
                UiKit.statTile("Total Users", String.valueOf(users.size()), "indigo"),
                UiKit.statTile("Creators", String.valueOf(creators), "blue"),
                UiKit.statTile("Viewers", String.valueOf(viewers), "green"),
                UiKit.statTile("Admins", String.valueOf(admins), "purple"),
                UiKit.statTile("Banned Users", String.valueOf(banned), "red"),
                UiKit.statTile("Published Content", String.valueOf(totalContent), "orange"),
                UiKit.statTile("Live Events", String.valueOf(totalEvents), "teal")
        );

        Label heading = new Label("Platform Overview");
        heading.getStyleClass().add("section-title");

        setContent(new VBox(16, heading, stats));
    }

    private static void showUsers() {
        Label heading = new Label("Manage Users");
        heading.getStyleClass().add("section-title");

        Button addAdminBtn = UiKit.primaryButton("+ Register Admin");
        addAdminBtn.setOnAction(e -> showRegisterAdminDialog());

        HBox header = new HBox(12, heading, spacerGrow(), addAdminBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TableView<User> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(roleOf(d.getValue())));

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isBanned() ? "Banned" : "Active"));

        TableColumn<User, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button actionBtn = new Button();
            {
                actionBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u == admin) return;
                    if (u.isBanned()) u.unban(); else admin.banUser(u);
                    showUsers();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                if (u == admin) { setGraphic(null); return; }
                actionBtn.setText(u.isBanned() ? "Unban" : "Ban");
                actionBtn.getStyleClass().setAll(u.isBanned() ? "btn-secondary" : "btn-danger", "btn-small");
                setGraphic(actionBtn);
            }
        });

        table.getColumns().addAll(nameCol, emailCol, roleCol, statusCol, actionCol);
        table.setItems(FXCollections.observableArrayList(ctx.getState().getUsers()));
        VBox.setVgrow(table, Priority.ALWAYS);

        setContent(new VBox(16, header, table));
    }

    private static String roleOf(User u) {
        if (u instanceof Administrator) return "Administrator";
        if (u instanceof Creator) return "Creator";
        if (u instanceof Viewer) return "Viewer";
        return "User";
    }

    private static void showRegisterAdminDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Administrator");
        dialog.getDialogPane().getStylesheets().add(
                AdminDashboardView.class.getResource("/stemplatform/stem/css/edusphere.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        TextField nameField = UiKit.field("Full name");
        TextField emailField = UiKit.field("Email address");
        PasswordField passwordField = UiKit.passwordField("Password");

        VBox box = new VBox(10, nameField, emailField, passwordField);
        box.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setResultConverter(bt -> bt);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    ctx.getAuthService().registerAdministrator(
                            nameField.getText().trim(), emailField.getText().trim(), passwordField.getText());
                    showUsers();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                }
            }
        });
    }

    private static void showContent() {
        Label heading = new Label("Manage Content");
        heading.getStyleClass().add("section-title");

        TableView<Content> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Content, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        TableColumn<Content, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClass().getSimpleName()));

        TableColumn<Content, String> creatorCol = new TableColumn<>("Creator");
        creatorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCreator().getName()));

        TableColumn<Content, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSubject()));

        TableColumn<Content, Number> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getViewCount()));

        TableColumn<Content, Number> likesCol = new TableColumn<>("Likes");
        likesCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getLikeCount()));

        TableColumn<Content, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.getStyleClass().addAll("btn-danger", "btn-small");
                removeBtn.setOnAction(e -> {
                    Content c = getTableView().getItems().get(getIndex());
                    admin.removeContent(ctx.getContentManager(), c);
                    showContent();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        table.getColumns().addAll(titleCol, typeCol, creatorCol, subjectCol, viewsCol, likesCol, actionCol);
        table.setItems(FXCollections.observableArrayList(ctx.getContentManager().getAllContent()));
        VBox.setVgrow(table, Priority.ALWAYS);

        setContent(new VBox(16, heading, table));
    }

    private static void showEvents() {
        Label heading = new Label("Live Events");
        heading.getStyleClass().add("section-title");

        TableView<LiveEvent> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LiveEvent, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        TableColumn<LiveEvent, String> hostCol = new TableColumn<>("Host");
        hostCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHost().getName()));

        TableColumn<LiveEvent, String> timeCol = new TableColumn<>("Start Time");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStartTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))));

        TableColumn<LiveEvent, String> capacityCol = new TableColumn<>("Viewers");
        capacityCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCurrentViewerCount() + " / " + d.getValue().getMaximumViewers()));

        table.getColumns().addAll(titleCol, hostCol, timeCol, capacityCol);
        table.setItems(FXCollections.observableArrayList(ctx.getState().getEvents()));
        VBox.setVgrow(table, Priority.ALWAYS);

        setContent(new VBox(16, heading, table));
    }

    private static Region spacerGrow() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private static void setContent(Node node) {
        content.getChildren().setAll(node);
    }
}