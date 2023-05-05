package chatting.controller;

import chatting.model.Chat;
import chatting.repository.ChannelRepository;
import chatting.service.RedisPublishService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class ChatController {

    private final RedisPublishService redisPublishService;
    private final ChannelRepository channelRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    //websocket "/pub/message"로 들어오는 메시징을 처리한다.
    @MessageMapping("/message")
    public void message(Chat chat) {
        chat.setMessageCd(UUID.randomUUID().toString());
        if (chat.getTransferType() == 1) {
            channelRepository.enterChannel(chat.getDomainCd(), chat.getChannelCd());
            chat.setMessage(chat.getWssCd() + "님이 입장하셨습니다.");
        }
        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        String destination = channelRepository.getTopic(chat.getDomainCd(), chat.getChannelCd()).getTopic();
        redisPublishService.publish(destination, chat);
    }

    @MessageMapping("/messageTo")
    public void messageTo(Chat chat) {
        System.out.println("messageTo : " + chat);
        chat.setMessageCd(UUID.randomUUID().toString());
        String userCd = chat.getToUserCd();
        ObjectMapper objectMapper = new ObjectMapper();
        Object toUserSessionObj = redisTemplate.opsForValue().get(userCd);
        List<String> userSessions = null;

        String destination = channelRepository.getTopic(chat.getDomainCd(), chat.getChannelCd()).getTopic();
        try{
            userSessions = objectMapper.readValue(
                    String.valueOf(toUserSessionObj)
                    , new TypeReference<ArrayList<String>>() {});
        } catch (Exception e) {
            userSessions = Collections.emptyList();
        }

        if(userSessions.isEmpty()){
            // 로그인하지 않은 상대일 시에 어떻게 할지 여기에 정의
            //chat.setMessage("상대방이 로그인하지 않았습니다.");
        }else{
            for (String sessionId : userSessions) {
                redisPublishService.publish(destination+"/"+sessionId, chat);
            }
        }
    }

    /*@EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        System.out.println("getMessage : "+event.getMessage());
        String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        System.out.println("Received a new web socket connection: " + sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("Web socket disconnected: " + sessionId);
    }*/
}