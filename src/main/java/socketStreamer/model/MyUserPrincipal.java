package socketStreamer.model;

import java.security.Principal;


public class MyUserPrincipal implements Principal {
    private String userId;
    public MyUserPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }
}
