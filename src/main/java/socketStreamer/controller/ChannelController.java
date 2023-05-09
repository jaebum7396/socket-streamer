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

    // 채팅방 입장 화면
    @GetMapping("/channel/enter/{domainCd}/{channelCd}")
    public String channelDetail(Model model,@PathVariable String domainCd, @PathVariable String channelCd) {
        log.info("channelDetail");
        model.addAttribute("domainCd", domainCd);
        model.addAttribute("channelCd", channelCd);
        return "/chatting/channel_detail";
    }

    // 모든 채팅방 목록 반환
    @GetMapping("/channels")
    @ResponseBody
    public List<Channel> channels(@RequestParam String domainCd) {
        log.info("channels");
        return channelRepository.findAllChannel(domainCd);
    }

    // 채팅방 생성
    @PostMapping("/channel")
    @ResponseBody
    public Channel createChannel(@RequestParam String domainCd, @RequestParam String channelCd) {
        log.info("createChannel");
        return channelRepository.createChannel(domainCd, channelCd);
    }

    // 특정 채팅방 조회
    @GetMapping("/channel/{domainCd}/{channelCd}")
    @ResponseBody
    public Channel channelInfo(@PathVariable String domainCd, @PathVariable String channelCd) {
        log.info("channelInfo");
        return channelRepository.getChannelByChannelCd(domainCd, channelCd);
    }
}