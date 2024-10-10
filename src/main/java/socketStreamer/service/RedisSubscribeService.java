package socketStreamer.service;

import socketStreamer.model.Chat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscribeService implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    // Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // redis에서 발행된 데이터를 받아 deserialize
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            // ChatMessage 객채로 맵핑
            /*Chat chat = objectMapper.readValue(publishMessage, Chat.class);
            if(!"".equals(chat.getToUser())&&chat.getToUser()!=null&&!"null".equals(chat.getToUser())){
                // 사용자 특정하여 채팅 메시지 Send
                System.out.println("directMessage : " + chat);
                messagingTemplate.convertAndSendToUser(chat.getToUser(), "/direct/"+chat.getDomainCd(), chat);
                //messagingTemplate.convertAndSend("/direct/user-pool", chat);
            }else{
                // 해당 토픽의 구독자 모두에게 채팅 메시지 Send
                System.out.println("broadCasting : " + chat);
                messagingTemplate.convertAndSend("/sub/channel/"+chat.getDomainCd()+"/"+chat.getChannelCd(), chat);
            }

            messagingTemplate.convertAndSend("/sub/channel/"+chat.getDomainCd()+"/"+chat.getChannelCd(), chat);
            */
            messagingTemplate.convertAndSend("/sub/channel/signalBroadCast", publishMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}