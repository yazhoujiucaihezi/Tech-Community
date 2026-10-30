package com.box.controller;

import com.box.common.Result;
import com.box.common.UserContext;
import com.box.dto.CommentDTO;
import com.box.dto.InteractDTO;
import com.box.dto.TopicUpdateDTO;
import com.box.entity.Topic;
import com.box.entity.TopicType;
import com.box.service.ForumService;
import com.box.service.WeatherService;
import com.box.vo.CommentVO;
import com.box.vo.TopicDetailVO;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛板块接口
 */
@RequestMapping("/api/forum")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final WeatherService weatherService;

    /**
     * 获取帖子类型
     */
    @GetMapping("/types")
    public Result<List<TopicType>> getTopicTypes() {
        return Result.success(forumService.getTopicTypes());
    }

    /**
     * 分页获取帖子列表
     */
    @GetMapping("/list-topic")
    public Result<List<Topic>> page(
            @RequestParam Integer page,
            @RequestParam(required = false) Integer type) {
        return Result.success(forumService.page(page, type));
    }

    /**
     * 获取置顶帖子列表
     */
    @GetMapping("/top-topic")
    public Result<List<Topic>> getTopTopic() {
        return Result.success(forumService.getTopTopic());
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/topic")
    public Result<TopicDetailVO> getTopic(@RequestParam Integer tid) {
        return Result.success(forumService.getTopic(UserContext.getUid(), tid));
    }

    /**
     * 帖子点赞/收藏
     */
    @PostMapping("/interactions")
    public Result<Void> interact(@RequestBody InteractDTO dto) {
        forumService.interact(UserContext.getUid(), dto.getTid(), dto.getType(), dto.getState());
        return Result.success(null);
    }

    /**
     * 获取帖子评论信息
     */
    @GetMapping("/comments")
    public Result<List<CommentVO>> getComments(@RequestParam Integer tid, @RequestParam Integer page) {
        return Result.success(forumService.getComments(tid, page));
    }

    /**
     * 添加帖子评论
     */
    @PostMapping("/add-comment")
    public Result<Void> addComment(@RequestBody CommentDTO commentDTO) {
        forumService.addComment(UserContext.getUid(), commentDTO);
        return Result.success(null);
    }

    /**
     * 删除帖子评论
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Integer id) {
        forumService.deleteComment(UserContext.getUid(), UserContext.getRole(), id);
        return Result.success(null);
    }

    /**
     * 修改帖子
     */
    @PostMapping("/update-topic")
    public Result<Void> updateTopic(@RequestBody TopicUpdateDTO dto) {
        forumService.updateTopic(UserContext.getRole(), UserContext.getUid(), dto);
        return Result.success(null);
    }

    /**
     * 创建帖子
     */
    @PostMapping("/create-topic")
    public Result<Void> createTopic(@RequestBody TopicUpdateDTO dto) {
        forumService.createTopic(UserContext.getUid(), dto);
        return Result.success(null);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/topics/{tid}")
    public Result<Void> deleteTopic(@PathVariable Integer tid) {
        forumService.deleteTopic(UserContext.getUid(), UserContext.getRole(), tid);
        return Result.success(null);
    }

    /** 获取当前用户的帖子列表 */
    @GetMapping("/user-topic")
    public Result<List<Topic>> getUserTopic() {
        return Result.success(forumService.getUserTopic(UserContext.getUid()));
    }

    /** 搜索帖子 */
    @GetMapping("/search-topic")
    public Result<List<Topic>> searchTopic(@RequestParam String keyword) {
        return Result.success(forumService.searchTopic(keyword));
    }

    /** 获取当前用户收藏的帖子 */
    @GetMapping("/collects")
    public Result<List<Topic>> getCollectsTopic() {
        return Result.success(forumService.getCollectsTopic(UserContext.getUid()));
    }

    /** 获取天气信息 */
    @GetMapping("/weather")
    public Result<JsonNode> weather(@RequestParam String longitude,
                                    @RequestParam String latitude) {
        return Result.success(weatherService.getWeather(longitude, latitude));
    }

    /** 获取热门帖子 */
    @GetMapping("hot-topic")
    public Result<List<Topic>> getHotTopic() {
        return Result.success(forumService.getHotTopic());
    }

}
