package stemplatform.stem;

import stemplatform.stem.authentication.AuthenticationService;
import stemplatform.stem.content.Article;
import stemplatform.stem.content.Comment;
import stemplatform.stem.content.Content;
import stemplatform.stem.content.Presentation;
import stemplatform.stem.content.ResearchPaper;
import stemplatform.stem.content.Video;
import stemplatform.stem.events.LiveEvent;
import stemplatform.stem.management.ContentManager;
import stemplatform.stem.management.DownloadManager;
import stemplatform.stem.notifications.NotificationService;
import stemplatform.stem.search.SearchEngine;
import stemplatform.stem.storage.ApplicationState;
import stemplatform.stem.storage.FileManager;
import stemplatform.stem.streaming.StreamingSession;
import stemplatform.stem.users.Administrator;
import stemplatform.stem.users.Creator;
import stemplatform.stem.users.User;
import stemplatform.stem.users.Viewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain-Java integration harness. No JavaFX, no GUI - this exists purely
 * to prove the services and managers work together correctly before any
 * screens get built on top of them. Run it as a normal Java application
 * (it does NOT extend javafx.application.Application), e.g.:
 *
 *   mvn compile exec:java -Dexec.mainClass="stemplatform.stem.IntegrationDemo"
 *
 * or just hit "Run" on this file's main() from your IDE.
 */
public class IntegrationDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {

        System.out.println("=== STEM PLATFORM INTEGRATION DEMO ===\n");

        // ------------------------------------------------------------
        // 1. Registration & authentication
        // ------------------------------------------------------------
        System.out.println("-- Authentication --");

        List<User> users = new ArrayList<>();
        AuthenticationService authService = new AuthenticationService(users);

        Creator creator = (Creator) authService.registerCreator(
                "Ali Khan", "ali@example.com", "password123", "STEM educator");
        Viewer viewer1 = (Viewer) authService.registerViewer(
                "Sara Ahmed", "sara@example.com", "password123");
        Viewer viewer2 = (Viewer) authService.registerViewer(
                "Bilal Raza", "bilal@example.com", "password123");
        Administrator admin = authService.registerAdministrator(
                "Admin One", "admin@example.com", "adminpass");

        check("4 users registered", users.size() == 4);

        User loggedIn = authService.login("ali@example.com", "password123");
        check("Creator can log in with correct credentials", loggedIn == creator);
        authService.logout();

        try {
            authService.login("ali@example.com", "wrongpass");
            check("Login with wrong password is rejected", false);
        } catch (IllegalArgumentException e) {
            check("Login with wrong password is rejected", true);
        }

        try {
            authService.registerViewer("Duplicate Sara", "sara@example.com", "password123");
            check("Duplicate email registration is rejected", false);
        } catch (IllegalArgumentException e) {
            check("Duplicate email registration is rejected", true);
        }

        try {
            authService.registerViewer("Short Pass", "shortpass@example.com", "123");
            check("Registration with a too-short password is rejected", false);
        } catch (IllegalArgumentException e) {
            check("Registration with a too-short password is rejected", true);
        }

        // ------------------------------------------------------------
        // 2. Publishing content
        // ------------------------------------------------------------
        System.out.println("\n-- Publishing content --");

        List<Content> allContent = new ArrayList<>();
        ContentManager contentManager = new ContentManager(allContent);

        Video video = new Video(
                "v1", creator, "Intro to Circuits",
                "Basics of electrical circuits.", "Electronics",
                "videos/intro-circuits.mp4", 720);
        contentManager.publishContent(creator, video);

        Article article = new Article(
                "a1", creator, "Understanding Ohm's Law",
                "A quick primer on Ohm's law.", "Electronics",
                "Ohm's law states that V = IR...", 5);
        contentManager.publishContent(creator, article);

        Path sharedFile = Files.createTempFile("stem-demo-file", ".pdf");
        Files.writeString(sharedFile, "Sample downloadable content for the demo.");

        ResearchPaper paper = new ResearchPaper(
                "r1", creator, "STEM Learning Outcomes",
                "Study on STEM engagement.", "Education",
                sharedFile.toString(),
                new String[] {"A. Khan", "B. Raza"},
                "This paper examines engagement across STEM programs.",
                "Journal of STEM Education",
                LocalDate.of(2025, 1, 15));
        contentManager.publishContent(creator, paper);

        Presentation slides = new Presentation(
                "p1", creator, "Circuit Basics Slides",
                "Slide deck for the circuits lesson.", "Electronics",
                sharedFile.toString(), 24, "Lecture");
        contentManager.publishContent(creator, slides);

        check("ContentManager tracks 4 published items", contentManager.getAllContent().size() == 4);
        check("Creator's own list also has 4 items", creator.getPublishedContent().size() == 4);

        try {
            contentManager.publishContent(creator, video);
            check("Publishing the same content twice is rejected", false);
        } catch (IllegalArgumentException e) {
            check("Publishing the same content twice is rejected", true);
        }

        // ------------------------------------------------------------
        // 3. Subscriptions & notifications
        // ------------------------------------------------------------
        System.out.println("\n-- Subscriptions & notifications --");

        viewer1.subscribe(creator);
        viewer2.subscribe(creator);
        viewer1.subscribe(creator); // re-subscribe should be a no-op
        check("Creator has exactly 2 subscribers", creator.getSubscribers().size() == 2);

        NotificationService notificationService = new NotificationService();
        notificationService.notifySubscribers(video);

        check("Viewer1 was notified", viewer1.getNotifications().size() == 1);
        check("Viewer2 was notified", viewer2.getNotifications().size() == 1);

        // ------------------------------------------------------------
        // 4. Watching video / StreamingSession
        // ------------------------------------------------------------
        System.out.println("\n-- Streaming --");

        StreamingSession session = viewer1.watch(video);
        check("watch() returns a session already playing", session.isPlaying());
        check("View count incremented on watch", video.getViewCount() == 1);
        check("Video recorded in viewer1's history", viewer1.getLibrary().getHistory().contains(video));

        session.pause();
        check("Session pauses", session.isPaused());
        session.resume();
        check("Session resumes", session.isPlaying());
        session.seek(300);
        check("Seek moves the playhead", session.getCurrentPosition() == 300);
        session.stop();
        check("Session stops", session.isStopped());

        viewer2.watch(video);
        check("A second viewer's watch records a second view", video.getViewCount() == 2);

        // ------------------------------------------------------------
        // 5. Likes, comments, library
        // ------------------------------------------------------------
        System.out.println("\n-- Likes, comments & library --");

        viewer1.like(article);
        viewer2.like(article);
        check("Article has 2 likes", article.getLikeCount() == 2);

        viewer1.unlike(article);
        check("Article has 1 like after unlike", article.getLikeCount() == 1);
        check("isLikedBy correctly reflects unlike", !article.isLikedBy(viewer1));

        viewer1.comment(article, "Great explanation, thanks!");
        check("Article has 1 comment", article.getComments().size() == 1);
        Comment postedComment = article.getComments().get(0);
        check("Comment author is viewer1", postedComment.getAuthor() == viewer1);

        viewer2.addFavorite(article);
        check("Article is in viewer2's favorites", viewer2.getLibrary().getFavorites().contains(article));

        viewer2.addToWatchLater(video);
        check("Video is in viewer2's watch-later", viewer2.getLibrary().getWatchLater().contains(video));
        viewer2.removeFromWatchLater(video);
        check("Video removed from watch-later", !viewer2.getLibrary().getWatchLater().contains(video));

        article.setTitle("Understanding Ohm's Law (Updated)");
        check("Content title can be edited", article.getTitle().equals("Understanding Ohm's Law (Updated)"));

        // ------------------------------------------------------------
        // 6. Live events & capacity limits
        // ------------------------------------------------------------
        System.out.println("\n-- Live events --");

        LiveEvent event = new LiveEvent(
                "e1", "Live Q&A: Circuits", LocalDateTime.now().plusDays(1), 1, creator);
        creator.createLiveEvent(event);
        check("Creator hosts 1 live event", creator.getLiveEvents().size() == 1);

        viewer1.attendEvent(event);
        check("Viewer1 joined the event", event.getCurrentViewerCount() == 1);

        viewer2.attendEvent(event);
        check("Viewer2 is rejected once the event is full", event.getCurrentViewerCount() == 1);
        check("Viewer2's own attended-events list stays empty", viewer2.getAttendedEvents().isEmpty());

        viewer1.leaveEvent(event);
        check("Viewer1 leaving frees up capacity", event.getCurrentViewerCount() == 0);

        // ------------------------------------------------------------
        // 7. Search
        // ------------------------------------------------------------
        System.out.println("\n-- Search --");

        SearchEngine searchEngine = new SearchEngine(contentManager.getAllContent());

        Content[] circuitResults = searchEngine.search("circuit");
        check("Search for 'circuit' finds the video and the slides",
                circuitResults.length == 2);

        Content[] bySubject = searchEngine.filterBySubject("Electronics");
        check("Filtering by 'Electronics' returns 3 items", bySubject.length == 3);

        Content[] byType = searchEngine.filterByType("Video");
        check("Filtering by type 'Video' returns exactly the video",
                byType.length == 1 && byType[0] instanceof Video);

        // ------------------------------------------------------------
        // 8. Downloading
        // ------------------------------------------------------------
        System.out.println("\n-- Downloads --");

        DownloadManager downloadManager = new DownloadManager();
        Path destination = Files.createTempFile("stem-demo-download", ".pdf");
        Files.deleteIfExists(destination);

        try {
            downloadManager.download(paper, destination.toString());
            check("Downloading a research paper writes the destination file",
                    Files.exists(destination));
        } catch (IOException e) {
            check("Downloading a research paper writes the destination file (" + e.getMessage() + ")", false);
        }

        // ------------------------------------------------------------
        // 9. Administrator moderation
        // ------------------------------------------------------------
        System.out.println("\n-- Administration --");

        admin.viewContent(video);

        admin.removeComment(article, postedComment);
        check("Admin can remove a comment", article.getComments().isEmpty());

        admin.removeContent(contentManager, slides);
        check("Admin-removed content leaves ContentManager",
                !contentManager.getAllContent().contains(slides));
        check("Admin-removed content leaves the creator's list too",
                !creator.getPublishedContent().contains(slides));

        admin.banUser(viewer2);
        check("Banning a user sets their banned flag", viewer2.isBanned());

        try {
            authService.login("bilal@example.com", "password123");
            check("A banned user cannot log in", false);
        } catch (IllegalStateException e) {
            check("A banned user cannot log in", true);
        }

        // ------------------------------------------------------------
        // 10. Persistence round-trip, including the LiveEvent fix
        // ------------------------------------------------------------
        System.out.println("\n-- Persistence (save/load) --");

        ApplicationState state = new ApplicationState();
        state.getUsers().addAll(users);
        state.getContent().addAll(contentManager.getAllContent());
        state.getEvents().add(event);

        FileManager fileManager = new FileManager();
        fileManager.save(state);

        ApplicationState loaded = fileManager.load();
        check("Reloaded state has the same number of users",
                loaded.getUsers().size() == state.getUsers().size());
        check("Reloaded state has the same number of content items",
                loaded.getContent().size() == state.getContent().size());
        check("Reloaded state has 1 live event", loaded.getEvents().size() == 1);

        LiveEvent reloadedEvent = loaded.getEvents().get(0);
        Viewer reloadedViewer = (Viewer) loaded.getUsers().stream()
                .filter(u -> u instanceof Viewer)
                .findFirst()
                .orElseThrow();

        try {
            boolean joined = reloadedEvent.join(reloadedViewer);
            check("Reloaded LiveEvent accepts join() without NPE "
                    + "(regression test for the transient currentViewers fix)", joined);
        } catch (NullPointerException npe) {
            check("Reloaded LiveEvent accepts join() without NPE "
                    + "(regression test for the transient currentViewers fix)", false);
        }

        // ------------------------------------------------------------
        System.out.println("\n========================================");
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        System.out.println("========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String label, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + label);
        } else {
            failed++;
            System.out.println("[FAIL] " + label);
        }
    }
}
