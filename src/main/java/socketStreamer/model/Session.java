package socketStreamer.model;

import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;

import java.util.Set;

public class Session implements SimpSession {
    private String sessionId;
    private SimpUser user;

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public SimpUser getUser() {
        return user;
    }

    @Override
    public Set<SimpSubscription> getSubscriptions() {
        return null;
    }
}
