package chatting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Channel implements Serializable {
    private static final long serialVersionUID = 6494678977089006639L;
    private String channelCd;
    private String domainCd;
    public static Channel create(String domainCd, String channelCd) {
        return Channel.builder().domainCd(domainCd).channelCd(channelCd).build();
    }
}
