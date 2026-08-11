package stemplatform.stem.notifications;

import stemplatform.stem.content.Content;
import stemplatform.stem.users.Creator;
import stemplatform.stem.users.Viewer;

public class NotificationService {

    public void notifySubscribers(Content content) {

        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        Creator creator = content.getCreator();

        String message = creator.getName()
                + " uploaded new content: "
                + content.getTitle();

        for (Viewer viewer : creator.getSubscribers()) {

            Notification notification =
                    new Notification(message, viewer);

            viewer.receiveNotification(notification);
        }
    }
}