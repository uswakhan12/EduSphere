package stemplatform.stem.gui;

import javafx.application.Platform;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import stemplatform.stem.content.Comment;
import stemplatform.stem.content.Content;
import stemplatform.stem.content.Downloadable;
import stemplatform.stem.content.Video;
import stemplatform.stem.events.LiveEvent;
import stemplatform.stem.notifications.Notification;
import stemplatform.stem.search.SearchEngine;
import stemplatform.stem.streaming.StreamingSession;
import stemplatform.stem.users.Creator;
import stemplatform.stem.users.Viewer;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ViewerDashboardView {

    private static AppContext ctx;
    private static Viewer viewer;
    private static StackPane content;
    private static Runnable currentRefresh = () -> {};
    private static Button navBrowse, navFavorites, navWatchLater, navHistory, navNotifications, navEvents, navSubscriptions;

    public static Parent build(AppContext appCtx, Viewer viewerUser) {
        ctx = appCtx;
        viewer = viewerUser;

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-bg");

        navBrowse = UiKit.navButton("Browse Content");
        navFavorites = UiKit.navButton("Favorites");
        navWatchLater = UiKit.navButton("Watch Later");
        navHistory = UiKit.navButton("History");
        navNotifications = UiKit.navButton("Notifications");
        navEvents = UiKit.navButton("Live Events");
        navSubscriptions = UiKit.navButton("Subscriptions");

        Label brand = new Label("EduSphere");
        brand.getStyleClass().add("sidebar-brand");
        Label sub = new Label("Learner Portal");
        sub.getStyleClass().add("sidebar-subtitle");
        Region gap = new Region();
        gap.setPrefHeight(18);

        VBox sidebar = new VBox(6, brand, sub, gap, navBrowse, navFavorites, navWatchLater,
                navHistory, navNotifications, navEvents, navSubscriptions);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(24, 14, 24, 14));
        root.setLeft(sidebar);

        HBox topBar = UiKit.topBar("Learner Portal", viewer.getName(), "VIEWER",
                () -> { ctx.getAuthService().logout(); ctx.setRoot(LoginView.build(ctx)); });

        content = new StackPane();
        content.setPadding(new Insets(24));
        VBox centerBox = new VBox(topBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.setCenter(centerBox);

        Button[] navs = { navBrowse, navFavorites, navWatchLater, navHistory, navNotifications, navEvents, navSubscriptions };
        navBrowse.setOnAction(e -> { showBrowse(); UiKit.setActiveNav(navBrowse, navs); });
        navFavorites.setOnAction(e -> { showFavorites(); UiKit.setActiveNav(navFavorites, navs); });
        navWatchLater.setOnAction(e -> { showWatchLater(); UiKit.setActiveNav(navWatchLater, navs); });
        navHistory.setOnAction(e -> { showHistory(); UiKit.setActiveNav(navHistory, navs); });
        navNotifications.setOnAction(e -> { showNotifications(); UiKit.setActiveNav(navNotifications, navs); });
        navEvents.setOnAction(e -> { showEvents(); UiKit.setActiveNav(navEvents, navs); });
        navSubscriptions.setOnAction(e -> { showSubscriptions(); UiKit.setActiveNav(navSubscriptions, navs); });

        showBrowse();
        UiKit.setActiveNav(navBrowse, navs);
        return root;
    }

    private static void showBrowse() {
        Label heading = new Label("Browse Content");
        heading.getStyleClass().add("section-title");

        TextField searchField = UiKit.field("Search by title, subject or description...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        Button searchBtn = UiKit.primaryButton("Search");

        VBox results = new VBox(12);
        ScrollPane scroll = new ScrollPane(results);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");

        Runnable refresh = () -> {
            List<Content> all = ctx.getContentManager().getAllContent();
            List<Content> shown;
            String query = searchField.getText();
            if (query == null || query.isBlank()) {
                shown = all;
            } else {
                SearchEngine engine = new SearchEngine(all);
                shown = Arrays.asList(engine.search(query));
            }
            results.getChildren().clear();
            if (shown.isEmpty()) {
                Label empty = new Label("No content found.");
                empty.getStyleClass().add("muted-text");
                results.getChildren().add(empty);
            } else {
                for (Content c : shown) results.getChildren().add(buildContentCard(c));
            }
        };
        currentRefresh = refresh;

        searchBtn.setOnAction(e -> refresh.run());
        searchField.setOnAction(e -> refresh.run());

        HBox searchBar = new HBox(10, searchField, searchBtn);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        refresh.run();

        VBox box = new VBox(16, heading, searchBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        setContent(box);
    }

    private static VBox buildContentCard(Content c) {
        Label titleLabel = new Label(c.getTitle());
        titleLabel.getStyleClass().add("list-item-title");
        Label typeBadge = UiKit.badge(c.getClass().getSimpleName(), "info");
        HBox titleRow = new HBox(10, titleLabel, typeBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label("By " + c.getCreator().getName() + "  •  " + c.getSubject()
                + "  •  " + c.getViewCount() + " views  •  " + c.getLikeCount() + " likes");
        meta.getStyleClass().add("muted-text");

        Label desc = new Label(c.getDescription());
        desc.getStyleClass().add("card-desc");
        desc.setWrapText(true);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        boolean liked = c.isLikedBy(viewer);
        Button likeBtn = UiKit.secondaryButton(liked ? "♥ Liked" : "♡ Like");
        likeBtn.setOnAction(e -> {
            if (c.isLikedBy(viewer)) viewer.unlike(c); else viewer.like(c);
            currentRefresh.run();
        });

        boolean favorited = viewer.getLibrary().getFavorites().contains(c);
        Button favBtn = UiKit.secondaryButton(favorited ? "★ Favorited" : "☆ Favorite");
        favBtn.setOnAction(e -> {
            if (viewer.getLibrary().getFavorites().contains(c)) viewer.removeFavorite(c); else viewer.addFavorite(c);
            currentRefresh.run();
        });

        boolean inWatchLater = viewer.getLibrary().getWatchLater().contains(c);
        Button watchLaterBtn = UiKit.secondaryButton(inWatchLater ? "✓ In Watch Later" : "+ Watch Later");
        watchLaterBtn.setOnAction(e -> {
            if (viewer.getLibrary().getWatchLater().contains(c)) viewer.removeFromWatchLater(c); else viewer.addToWatchLater(c);
            currentRefresh.run();
        });

        Button commentBtn = UiKit.secondaryButton("Comment (" + c.getComments().size() + ")");
        commentBtn.setOnAction(e -> showCommentDialog(c));

        actions.getChildren().addAll(likeBtn, favBtn, watchLaterBtn, commentBtn);

        if (c instanceof Video video) {
            Button watchBtn = UiKit.primaryButton("▶ Watch");
            watchBtn.setOnAction(e -> showWatchDialog(video));
            actions.getChildren().add(watchBtn);
        }

        if (c instanceof Downloadable downloadable) {
            Button downloadBtn = UiKit.primaryButton("⬇ Download");
            downloadBtn.setOnAction(e -> handleDownload(downloadable, c.getTitle()));
            actions.getChildren().add(downloadBtn);
        }

        Creator creatorOfContent = c.getCreator();
        boolean subscribed = viewer.getSubscriptions().contains(creatorOfContent);
        Button subBtn = UiKit.ghostButton(subscribed ? "Unsubscribe from " + creatorOfContent.getName()
                : "Subscribe to " + creatorOfContent.getName());
        subBtn.setOnAction(e -> {
            if (viewer.getSubscriptions().contains(creatorOfContent)) viewer.unsubscribe(creatorOfContent);
            else viewer.subscribe(creatorOfContent);
            currentRefresh.run();
        });

        return UiKit.card(titleRow, meta, desc, actions, subBtn);
    }

    private static void showCommentDialog(Content c) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Comment on: " + c.getTitle());
        dialog.getDialogPane().getStylesheets().add(
                ViewerDashboardView.class.getResource("/stemplatform/stem/css/edusphere.css").toExternalForm());

        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        for (Comment cm : c.getComments()) {
            Label l = new Label(cm.getAuthor().getName() + ": " + cm.getText());
            l.setWrapText(true);
            box.getChildren().add(l);
        }
        TextArea newComment = UiKit.textArea("Write a comment...", 3);
        box.getChildren().add(newComment);

        dialog.getDialogPane().setContent(new ScrollPane(box));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setResultConverter(bt -> bt);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK && !newComment.getText().isBlank()) {
                viewer.comment(c, newComment.getText());
                currentRefresh.run();
            }
        });
    }

    private static void showWatchDialog(Video video) {
        StreamingSession session = viewer.watch(video);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Now Watching: " + video.getTitle());
        dialog.getDialogPane().getStylesheets().add(
                ViewerDashboardView.class.getResource("/stemplatform/stem/css/edusphere.css").toExternalForm());

        Label status = new Label(statusText(session, video));

        Button playBtn = UiKit.primaryButton("Play / Resume");
        Button pauseBtn = UiKit.secondaryButton("Pause");
        Button stopBtn = UiKit.dangerButton("Stop");

        playBtn.setOnAction(e -> {
            if (session.isPaused()) session.resume(); else session.start();
            status.setText(statusText(session, video));
        });
        pauseBtn.setOnAction(e -> { session.pause(); status.setText(statusText(session, video)); });
        stopBtn.setOnAction(e -> { session.stop(); status.setText(statusText(session, video)); });

        HBox controls = new HBox(10, playBtn, pauseBtn, stopBtn);
        VBox box = new VBox(14, status, controls);
        box.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
        currentRefresh.run();
    }

    private static String statusText(StreamingSession session, Video video) {
        return "Status: " + session.getStatus() + "   Position: " + session.getCurrentPosition() + "s / " + video.getDuration() + "s";
    }

    private static void handleDownload(Downloadable downloadable, String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save '" + title + "' as...");
        File dest = chooser.showSaveDialog(ctx.getPrimaryStage());
        if (dest == null) return;

        // Runs on a background thread pool (DownloadManager.downloadExecutor)
        // so the UI stays responsive while the file is being copied.
        // The callbacks fire on that background thread, so any touch of
        // JavaFX nodes (the Alert dialog) is wrapped in Platform.runLater
        // to hop back onto the JavaFX Application Thread.
        ctx.getDownloadManager().downloadAsync(
                downloadable,
                dest.getAbsolutePath(),
                path -> Platform.runLater(() ->
                        new Alert(Alert.AlertType.INFORMATION,
                                "Downloaded to " + path).showAndWait()),
                ex -> Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR,
                                "Download failed: " + ex.getMessage()).showAndWait())
        );
    }

    private static void showFavorites() {
        showContentList("My Favorites");
    }

    private static void showWatchLater() {
        showContentList("Watch Later");
    }

    private static void showHistory() {
        showContentList("Watch History");
    }

    private static void showContentList(String title) {
        Label heading = new Label(title);
        heading.getStyleClass().add("section-title");

        VBox results = new VBox(12);
        ScrollPane scroll = new ScrollPane(results);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");

        Runnable refresh = () -> {
            List<Content> current = switch (title) {
                case "My Favorites" -> new ArrayList<>(viewer.getLibrary().getFavorites());
                case "Watch Later" -> new ArrayList<>(viewer.getLibrary().getWatchLater());
                default -> new ArrayList<>(viewer.getLibrary().getHistory());
            };
            results.getChildren().clear();
            if (current.isEmpty()) {
                Label empty = new Label("Nothing here yet.");
                empty.getStyleClass().add("muted-text");
                results.getChildren().add(empty);
            } else {
                for (Content c : current) results.getChildren().add(buildContentCard(c));
            }
        };
        currentRefresh = refresh;
        refresh.run();

        VBox box = new VBox(16, heading, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        setContent(box);
    }

    private static void showNotifications() {
        Label heading = new Label("Notifications");
        heading.getStyleClass().add("section-title");

        VBox list = new VBox(10);
        Runnable refresh = () -> {
            list.getChildren().clear();
            List<Notification> notifications = new ArrayList<>(viewer.getNotifications());
            notifications.sort(Comparator.comparing(Notification::getDate).reversed());
            if (notifications.isEmpty()) {
                Label empty = new Label("You have no notifications.");
                empty.getStyleClass().add("muted-text");
                list.getChildren().add(empty);
            }
            for (Notification n : notifications) {
                Label msg = new Label(n.getMessage());
                msg.setWrapText(true);
                Label date = new Label(n.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
                date.getStyleClass().add("muted-text");
                HBox row = new HBox(12, msg, date);
                if (!n.isRead()) {
                    Button markRead = UiKit.ghostButton("Mark as read");
                    markRead.setOnAction(e -> { n.markAsRead(); currentRefresh.run(); });
                    row.getChildren().add(markRead);
                } else {
                    row.getChildren().add(UiKit.badge("Read", "success"));
                }
                row.setAlignment(Pos.CENTER_LEFT);
                list.getChildren().add(UiKit.card(row));
            }
        };
        currentRefresh = refresh;
        refresh.run();

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");

        VBox box = new VBox(16, heading, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        setContent(box);
    }

    private static void showEvents() {
        Label heading = new Label("Live Events");
        heading.getStyleClass().add("section-title");

        VBox list = new VBox(10);
        Runnable refresh = () -> {
            list.getChildren().clear();
            List<LiveEvent> events = ctx.getState().getEvents();
            if (events.isEmpty()) {
                Label empty = new Label("No live events scheduled.");
                empty.getStyleClass().add("muted-text");
                list.getChildren().add(empty);
            }
            for (LiveEvent ev : events) {
                Label title = new Label(ev.getTitle());
                title.getStyleClass().add("list-item-title");
                Label meta = new Label("Host: " + ev.getHost().getName() + "  •  "
                        + ev.getStartTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                        + "  •  " + ev.getCurrentViewerCount() + "/" + ev.getMaximumViewers() + " viewers");
                meta.getStyleClass().add("muted-text");

                boolean attending = viewer.getAttendedEvents().contains(ev);
                Button joinBtn = attending ? UiKit.dangerButton("Leave Event") : UiKit.primaryButton("Join Event");
                joinBtn.setOnAction(e -> {
                    if (viewer.getAttendedEvents().contains(ev)) viewer.leaveEvent(ev); else viewer.attendEvent(ev);
                    currentRefresh.run();
                });

                list.getChildren().add(UiKit.card(title, meta, joinBtn));
            }
        };
        currentRefresh = refresh;
        refresh.run();

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");

        VBox box = new VBox(16, heading, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        setContent(box);
    }

    private static void showSubscriptions() {
        Label heading = new Label("My Subscriptions");
        heading.getStyleClass().add("section-title");

        VBox list = new VBox(10);
        Runnable refresh = () -> {
            list.getChildren().clear();
            Set<Creator> subs = viewer.getSubscriptions();
            if (subs.isEmpty()) {
                Label empty = new Label("You are not subscribed to any creators yet.");
                empty.getStyleClass().add("muted-text");
                list.getChildren().add(empty);
            }
            for (Creator cr : subs) {
                Label name = new Label(cr.getName());
                name.getStyleClass().add("list-item-title");
                Label bio = new Label(cr.getBio() == null ? "" : cr.getBio());
                bio.getStyleClass().add("muted-text");
                bio.setWrapText(true);
                Button unsub = UiKit.dangerButton("Unsubscribe");
                unsub.setOnAction(e -> { viewer.unsubscribe(cr); currentRefresh.run(); });
                list.getChildren().add(UiKit.card(name, bio, unsub));
            }
        };
        currentRefresh = refresh;
        refresh.run();

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");

        VBox box = new VBox(16, heading, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        setContent(box);
    }

    private static void setContent(Node node) {
        content.getChildren().setAll(node);
    }
}