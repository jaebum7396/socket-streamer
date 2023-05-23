package socketStreamer.controller;

import socketStreamer.model.Channel;
import socketStreamer.repository.ChannelRepository;
import socketStreamer.service.RedisPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@Slf4j
public class ChannelController {

    private final RedisPublishService redisPublishService;
    private final ChannelRepository channelRepository;

    // 채팅 리스트 화면
    @GetMapping("/channel")
    public String channels(Model model) {
        log.info("channels");
        return "/chatting/channel";
    }
}