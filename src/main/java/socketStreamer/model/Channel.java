package socketStreamer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Channel implements Serializable {
    private static final long serialVersionUID = 6494678977089006639L;
    private String channelCd;
    private String domainCd;
    public static Channel create(String domainCd, String channelCd) {
        return Channel.builder().domainCd(domainCd).channelCd(channelCd).build();
    }
}
