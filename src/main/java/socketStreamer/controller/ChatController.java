package socketStreamer.controller;

import socketStreamer.model.Chat;
import socketStreamer.repository.ChannelRepository;
import socketStreamer.service.RedisPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ChatController {

    private final RedisPublishService redisPublishService;
    private final ChannelRepository channelRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //websocket "/pub/message"로 들어오는 메시징을 처리한다.
    @MessageMapping("/message")
    public void message(Chat chat) {
        System.out.println(chat);
        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        String destination = channelRepository.getTopic(chat.getDomainCd()).getTopic();
        redisPublishService.publish(destination, chat);
    }

    @MessageMapping("/enter")
    public void enter(Chat chat) {
        System.out.println(chat);
        if (chat.getTransferType() == 1) {
            channelRepository.enterTopic(chat.getDomainCd());
        }
    }
}