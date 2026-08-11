package stemplatform.stem.storage;

import stemplatform.stem.content.Content;
import stemplatform.stem.events.LiveEvent;
import stemplatform.stem.users.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationState implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<User> users;
    private List<Content> content;
    private List<LiveEvent> events;

    public ApplicationState() {
        users = new ArrayList<>();
        content = new ArrayList<>();
        events = new ArrayList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Content> getContent() {
        return content;
    }

    public List<LiveEvent> getEvents() {
        return events;
    }
}