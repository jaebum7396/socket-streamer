package socketStreamer.model;

import java.security.Principal;


public class MyUserPrincipal implements Principal {
    private String userCd;
    public MyUserPrincipal(String userCd) {
        this.userCd = userCd;
    }

    @Override
    public String getName() {
        return userCd;
    }
}
