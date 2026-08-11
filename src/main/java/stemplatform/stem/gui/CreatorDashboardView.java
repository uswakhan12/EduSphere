package stemplatform.stem.gui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import stemplatform.stem.content.Article;
import stemplatform.stem.content.Content;
import stemplatform.stem.content.Presentation;
import stemplatform.stem.content.ResearchPaper;
import stemplatform.stem.content.Video;
import stemplatform.stem.events.LiveEvent;
import stemplatform.stem.users.Creator;
import stemplatform.stem.users.Viewer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CreatorDashboardView {

    private static AppContext ctx;
    private static Creator creator;
    private static StackPane content;
    private static Button navOverview, navMyContent, navPublish, navEvents, navSubscribers;

    public static Parent build(AppContext appCtx, Creator creatorUser) {
        ctx = appCtx;
        creator = creatorUser;

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-bg");

        navOverview = UiKit.navButton("Overview");
        navMyContent = UiKit.navButton("My Content");
        navPublish = UiKit.navButton("Publish New");
        navEvents = UiKit.navButton("Live Events");
        navSubscribers = UiKit.navButton("Subscribers");

        Label brand = new Label("EduSphere");
        brand.getStyleClass().add("sidebar-brand");
        Label sub = new Label("Creator Studio");
        sub.getStyleClass().add("sidebar-subtitle");
        Region gap = new Region();
        gap.setPrefHeight(18);

        VBox sidebar = new VBox(6, brand, sub, gap, navOverview, navMyContent, navPublish, navEvents, navSubscribers);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(24, 14, 24, 14));
        root.setLeft(sidebar);

        HBox topBar = UiKit.topBar("Creator Studio", creator.getName(), "CREATOR",
                () -> { ctx.getAuthService().logout(); ctx.setRoot(LoginView.build(ctx)); });

        content = new StackPane();
        content.setPadding(new Insets(24));
        VBox centerBox = new VBox(topBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.setCenter(centerBox);

        Button[] navs = { navOverview, navMyContent, navPublish, navEvents, navSubscribers };
        navOverview.setOnAction(e -> { showOverview(); UiKit.setActiveNav(navOverview, navs); });
        navMyContent.setOnAction(e -> { showMyContent(); UiKit.setActiveNav(navMyContent, navs); });
        navPublish.setOnAction(e -> { showPublish(); UiKit.setActiveNav(navPublish, navs); });
        navEvents.setOnAction(e -> { showEvents(); UiKit.setActiveNav(navEvents, navs); });
        navSubscribers.setOnAction(e -> { showSubscribers(); UiKit.setActiveNav(navSubscribers, navs); });

        showOverview();
        UiKit.setActiveNav(navOverview, navs);
        return root;
    }

    private static void showOverview() {
        int published = creator.getPublishedContent().size();
        int subscribers = creator.getSubscribers().size();
        int totalViews = creator.getPublishedContent().stream().mapToInt(Content::getViewCount).sum();
        int totalLikes = creator.getPublishedContent().stream().mapToInt(Content::getLikeCount).sum();
        int events = creator.getLiveEvents().size();

        FlowPane stats = new FlowPane(16, 16);
        stats.getChildren().addAll(
                UiKit.statTile("Published Content", String.valueOf(published), "indigo"),
                UiKit.statTile("Subscribers", String.valueOf(subscribers), "blue"),
                UiKit.statTile("Total Views", String.valueOf(totalViews), "green"),
                UiKit.statTile("Total Likes", String.valueOf(totalLikes), "orange"),
                UiKit.statTile("Live Events", String.valueOf(events), "teal")
        );

        Label heading = new Label("Welcome back, " + creator.getName());
        heading.getStyleClass().add("section-title");
        Label bio = new Label(creator.getBio() == null || creator.getBio().isBlank()
                ? "No bio yet." : creator.getBio());
        bio.getStyleClass().add("muted-text");
        bio.setWrapText(true);

        setContent(new VBox(16, heading, bio, stats));
    }

    private static void showMyContent() {
        Label heading = new Label("My Content");
        heading.getStyleClass().add("section-title");

        TableView<Content> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Content, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        TableColumn<Content, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClass().getSimpleName()));

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
                    ctx.getContentManager().removeContent(c);
                    showMyContent();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        table.getColumns().addAll(titleCol, typeCol, subjectCol, viewsCol, likesCol, actionCol);
        table.setItems(FXCollections.observableArrayList(creator.getPublishedContent()));
        VBox.setVgrow(table, Priority.ALWAYS);

        setContent(new VBox(16, heading, table));
    }

    private static void showPublish() {
        Label heading = new Label("Publish New Content");
        heading.getStyleClass().add("section-title");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Video", "Article", "Research Paper", "Presentation"));
        typeBox.setValue("Video");
        typeBox.getStyleClass().add("input-field");

        TextField titleField = UiKit.field("Title");
        TextField subjectField = UiKit.field("Subject (e.g. Physics, Programming)");
        TextArea descriptionArea = UiKit.textArea("Short description", 3);

        VBox dynamicFields = new VBox(10);

        TextField filePathField = UiKit.field("File path (e.g. files/lesson1.mp4)");
        TextField durationField = UiKit.field("Duration in seconds");
        TextArea bodyArea = UiKit.textArea("Article body", 5);
        TextField readingTimeField = UiKit.field("Reading time (minutes)");
        TextField authorsField = UiKit.field("Authors (comma separated)");
        TextArea abstractArea = UiKit.textArea("Abstract", 4);
        TextField publicationField = UiKit.field("Publication name");
        DatePicker publicationDatePicker = new DatePicker(LocalDate.now());
        publicationDatePicker.getStyleClass().add("input-field");
        TextField slideCountField = UiKit.field("Slide count");
        TextField presentationTypeField = UiKit.field("Presentation type (e.g. Lecture, Workshop)");

        Runnable refreshFields = () -> {
            dynamicFields.getChildren().clear();
            switch (typeBox.getValue()) {
                case "Video" -> dynamicFields.getChildren().addAll(filePathField, durationField);
                case "Article" -> dynamicFields.getChildren().addAll(bodyArea, readingTimeField);
                case "Research Paper" -> dynamicFields.getChildren().addAll(
                        filePathField, authorsField, abstractArea, publicationField, publicationDatePicker);
                case "Presentation" -> dynamicFields.getChildren().addAll(
                        filePathField, slideCountField, presentationTypeField);
            }
        };
        typeBox.setOnAction(e -> refreshFields.run());
        refreshFields.run();

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        Button publishBtn = UiKit.primaryButton("Publish Content");
        publishBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            try {
                String id = UUID.randomUUID().toString();
                Content newContent;
                switch (typeBox.getValue()) {
                    case "Video" -> newContent = new Video(id, creator, titleField.getText(),
                            descriptionArea.getText(), subjectField.getText(),
                            filePathField.getText(), Integer.parseInt(durationField.getText().trim()));
                    case "Article" -> newContent = new Article(id, creator, titleField.getText(),
                            descriptionArea.getText(), subjectField.getText(),
                            bodyArea.getText(), Integer.parseInt(readingTimeField.getText().trim()));
                    case "Research Paper" -> newContent = new ResearchPaper(id, creator, titleField.getText(),
                            descriptionArea.getText(), subjectField.getText(), filePathField.getText(),
                            authorsField.getText().split("\\s*,\\s*"), abstractArea.getText(),
                            publicationField.getText(), publicationDatePicker.getValue());
                    case "Presentation" -> newContent = new Presentation(id, creator, titleField.getText(),
                            descriptionArea.getText(), subjectField.getText(), filePathField.getText(),
                            Integer.parseInt(slideCountField.getText().trim()), presentationTypeField.getText());
                    default -> throw new IllegalStateException("Unknown content type");
                }
                ctx.getContentManager().publishContent(creator, newContent);
                ctx.getNotificationService().notifySubscribers(newContent);

                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "'" + newContent.getTitle() + "' has been published.");
                alert.setHeaderText("Content Published");
                alert.showAndWait();

                showMyContent();
                UiKit.setActiveNav(navMyContent, navOverview, navMyContent, navPublish, navEvents, navSubscribers);
            } catch (NumberFormatException nfe) {
                errorLabel.setText("Please enter valid numeric values.");
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        VBox form = UiKit.card(typeBox, titleField, subjectField, descriptionArea, dynamicFields, errorLabel, publishBtn);
        form.setMaxWidth(560);

        setContent(new VBox(16, heading, form));
    }

    private static void showEvents() {
        Label heading = new Label("Live Events");
        heading.getStyleClass().add("section-title");

        TableView<LiveEvent> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LiveEvent, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        TableColumn<LiveEvent, String> timeCol = new TableColumn<>("Start Time");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStartTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))));

        TableColumn<LiveEvent, String> capacityCol = new TableColumn<>("Viewers");
        capacityCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCurrentViewerCount() + " / " + d.getValue().getMaximumViewers()));

        table.getColumns().addAll(titleCol, timeCol, capacityCol);
        table.setItems(FXCollections.observableArrayList(creator.getLiveEvents()));
        table.setPrefHeight(220);

        TextField eventTitleField = UiKit.field("Event title");
        DatePicker eventDatePicker = new DatePicker(LocalDate.now());
        eventDatePicker.getStyleClass().add("input-field");
        TextField eventTimeField = UiKit.field("Start time (HH:mm, e.g. 18:30)");
        TextField maxViewersField = UiKit.field("Maximum viewers");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setVisible(false);

        Button createBtn = UiKit.primaryButton("Schedule Event");
        createBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            try {
                LocalTime time = LocalTime.parse(eventTimeField.getText().trim());
                LocalDateTime startDateTime = LocalDateTime.of(eventDatePicker.getValue(), time);
                int maxViewers = Integer.parseInt(maxViewersField.getText().trim());

                LiveEvent event = new LiveEvent(UUID.randomUUID().toString(),
                        eventTitleField.getText(), startDateTime, maxViewers, creator);
                creator.createLiveEvent(event);
                ctx.getState().getEvents().add(event);

                showEvents();
                UiKit.setActiveNav(navEvents, navOverview, navMyContent, navPublish, navEvents, navSubscribers);
            } catch (Exception ex) {
                errorLabel.setText("Check your inputs: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        Label formTitle = new Label("Schedule a new live event");
        formTitle.getStyleClass().add("card-title");
        VBox form = UiKit.card(formTitle, eventTitleField, eventDatePicker, eventTimeField, maxViewersField, errorLabel, createBtn);
        form.setMaxWidth(420);

        HBox layout = new HBox(20, table, form);
        HBox.setHgrow(table, Priority.ALWAYS);

        setContent(new VBox(16, heading, layout));
    }

    private static void showSubscribers() {
        Label heading = new Label("Subscribers (" + creator.getSubscribers().size() + ")");
        heading.getStyleClass().add("section-title");

        ListView<Viewer> list = new ListView<>(FXCollections.observableArrayList(creator.getSubscribers()));
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Viewer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setGraphic(null); return; }
                Label name = new Label(v.getName());
                name.getStyleClass().add("list-item-title");
                Label email = new Label(v.getEmail());
                email.getStyleClass().add("muted-text");
                setGraphic(new VBox(2, name, email));
            }
        });
        list.getStyleClass().add("data-list");
        VBox.setVgrow(list, Priority.ALWAYS);

        setContent(new VBox(16, heading, list));
    }

    private static void setContent(Node node) {
        content.getChildren().setAll(node);
    }
}