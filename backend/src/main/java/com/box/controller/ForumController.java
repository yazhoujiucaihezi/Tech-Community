package com.box.controller;

import com.box.common.Result;
import com.box.dto.CommentDTO;
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
import com.box.utils.JwtUtils;

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
    private final JwtUtils jwtUtils;

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
    public Result<TopicDetailVO> getTopic(@RequestHeader("Authorization") String authHeader, @RequestParam Integer tid) {
        return Result.success(forumService.getTopic(jwtUtils.getUid(authHeader), tid));
    }

    /**
     * 帖子点赞/收藏
     */
    @GetMapping("/interact")
    public Result<Void> interact(@RequestHeader("Authorization") String authHeader,
                                 @RequestParam Integer tid,
                                 @RequestParam String type,
                                 @RequestParam Boolean state) {
        forumService.interact(jwtUtils.getUid(authHeader), tid, type, state);
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
    public Result<Void> addComment(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody CommentDTO commentDTO) {
        forumService.addComment(jwtUtils    .getUid(authHeader), commentDTO);
        return Result.success(null);
    }

    /**
     * 删除帖子评论
     */
    @GetMapping("/delete-comment")
    public Result<Void> deleteComment(@RequestHeader("Authorization") String authHeader,
                                      @RequestParam Integer id) {
        forumService.deleteComment(jwtUtils.getUid(authHeader), jwtUtils.getRole(authHeader), id);
        return Result.success(null);
    }

    /**
     * 修改帖子
     */
    @PostMapping("/update-topic")
    public Result<Void> updateTopic(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody TopicUpdateDTO dto) {
        forumService.updateTopic(jwtUtils.getRole(authHeader), jwtUtils.getUid(authHeader), dto);
        return Result.success(null);
    }

    /**
     * 创建帖子
     */
    @PostMapping("/create-topic")
    public Result<Void> createTopic(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody TopicUpdateDTO dto) {
        forumService.createTopic(jwtUtils.getUid(authHeader), dto);
        return Result.success(null);
    }

    /**
     * 删除帖子
     */
    @GetMapping("/delete-topic")
    public Result<Void> deleteTopic(@RequestHeader("Authorization") String authHeader,
                                    @RequestParam Integer tid) {
        forumService.deleteTopic(jwtUtils.getUid(authHeader), jwtUtils.getRole(authHeader), tid);
        return Result.success(null);
    }

    /** 获取当前用户的帖子列表 */
    @GetMapping("/user-topic")
    public Result<List<Topic>> getUserTopic(@RequestHeader("Authorization") String authHeader) {
        return Result.success(forumService.getUserTopic(jwtUtils.getUid(authHeader)));
    }

    /** 搜索帖子 */
    @GetMapping("/search-topic")
    public Result<List<Topic>> searchTopic(@RequestParam String keyword) {
        return Result.success(forumService.searchTopic(keyword));
    }

    /** 获取当前用户收藏的帖子 */
    @GetMapping("/collects")
    public Result<List<Topic>> getCollectsTopic(@RequestHeader("Authorization") String authHeader) {
        return Result.success(forumService.getCollectsTopic(jwtUtils.getUid(authHeader)));
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
