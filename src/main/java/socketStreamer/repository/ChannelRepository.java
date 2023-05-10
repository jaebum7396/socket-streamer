package socketStreamer.repository;

import socketStreamer.model.Channel;
import socketStreamer.service.RedisSubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ChannelRepository {
    // 채팅방(topic)에 발행되는 메시지를 처리할 Listener
    private final RedisMessageListenerContainer redisMessageListener;
    // 구독 처리 서비스
    private final RedisSubscribeService redisSubscribeService;
    // Redis
    private static final String CHANNELS = "CHANNEL";

    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, Channel> opsHashChannel;
    // 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보. 서버별로 채팅방에 매치되는 topic정보를 Map에 넣어 channelCd로 찾을수 있도록 한다.
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        log.info("init");
        opsHashChannel = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }
    public List<Channel> findAllChannel(String domainCd) {
        log.info("findAllChannel");
        return opsHashChannel.values(domainCd);
    }
    public Channel getChannelByChannelCd(String domainCd, String channelCd) {
        log.info("getChannelByChannelCd");
        return opsHashChannel.get(domainCd, channelCd);
    }
    /**
     * 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
     */
    public Channel createChannel(String domainCd, String channelCd) {
        log.info("createChannel");
        Channel channel = Channel.create(domainCd, channelCd);
        opsHashChannel.put(domainCd, channel.getChannelCd(), channel);
        return channel;
    }
    /**
     * 채팅방 입장 : redis에 topic을 만들고 pub/sub 통신을 하기 위해 리스너를 설정한다.
     */
    public void enterChannel(String domainCd, String channelCd) {
        log.info("enterChannel : " + domainCd+"-"+channelCd);
        ChannelTopic topic = topics.get(domainCd+"-"+channelCd);
        if (topic == null) {
            log.info("채널 생성 : " + domainCd+"-"+channelCd);
            topic = new ChannelTopic(domainCd+"-"+channelCd);
            redisMessageListener.addMessageListener(redisSubscribeService, topic);
            topics.put(domainCd+"-"+channelCd, topic);
        }
    }
    public ChannelTopic getTopic(String domainCd, String channelCd) {
        System.out.println("allTopics: " + topics);
        System.out.println("domainCd: " + domainCd);
        System.out.println("channelCd: " + channelCd);
        if(topics.get(domainCd+"-"+channelCd) == null){
            log.info("채널 생성 : " + domainCd+"-"+channelCd);
            ChannelTopic topic = new ChannelTopic(domainCd+"-"+channelCd);
            redisMessageListener.addMessageListener(redisSubscribeService, topic);
            topics.put(domainCd+"-"+channelCd, topic);
        }
        log.info("getTopic: " + topics.get(domainCd+"-"+channelCd).toString());
        return topics.get(domainCd+"-"+channelCd);
    }
}
