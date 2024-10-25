package socketStreamer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import socketStreamer.model.Envelope;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscribeService implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    @Override
    public void onMessage(Message envelopeMessage, byte[] pattern) {
        try {
            // redis에서 발행된 데이터를 받아 deserialize
            String envelopeString = (String) redisTemplate.getStringSerializer().deserialize(envelopeMessage.getBody());


            // 만약 publishMessage가 null이면 early return
            if (envelopeString == null) {
                log.error("Received null message");
                return;
            }
            // 앞뒤 큰따옴표로 감싸져 있다면 제거
            if (envelopeString.startsWith("\"") && envelopeString.endsWith("\"")) {
                envelopeString = envelopeString.substring(1, envelopeString.length() - 1);
            }

            Envelope envelope = objectMapper.readValue(envelopeString, Envelope.class);
            String topic = envelope.getTopic();
            Object payload = envelope.getPayload();
            String payloadString = objectMapper.writeValueAsString(payload);
            /*if(!"".equals(chat.getToUser())&&chat.getToUser()!=null&&!"null".equals(chat.getToUser())){
                // 사용자 특정하여 채팅 메시지 Send
                System.out.println("directMessage : " + chat);
                messagingTemplate.convertAndSendToUser(chat.getToUser(), "/direct/"+chat.getDomainCd(), chat);
                //messagingTemplate.convertAndSend("/direct/user-pool", chat);
            }else{
                // 해당 토픽의 구독자 모두에게 채팅 메시지 Send
                System.out.println("broadCasting : " + chat);
                messagingTemplate.convertAndSend("/sub/channel/"+chat.getDomainCd()+"/"+chat.getChannelCd(), chat);
            }
            */
            messagingTemplate.convertAndSend("/sub/channel/"+topic, payloadString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}