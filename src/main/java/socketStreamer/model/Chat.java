package socketStreamer.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Chat {
    // 메시지 타입 : 입장, 채팅
    private String chatCd; // 메시지코드
    private String channelCd; // 방번호
    private String domainCd; // 도메인코드
    private String userCd; // 유저코드
    private int transferType; // 전송타입(1: 접속, 2: 접속해제, 3: 나가기, 4: 채팅, 5: 귓속말, 6: 공지, 7: 알림, 8: 시스템)
    private int messageType; // 메시지타입(1: TEXT, 2: IMAGE, 3: 영상, 4:파일, 5: 링크, 6: 이모티콘)
    private LocalDateTime messageDt;
    private String message; // 메시지
    private String toUser; // 귓속말 대상자
}
