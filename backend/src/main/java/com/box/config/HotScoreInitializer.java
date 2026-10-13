package com.box.config;

import com.box.entity.Topic;
import com.box.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HotScoreInitializer implements CommandLineRunner {

    private final TopicMapper topicMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) {
        List<Topic> allTopics = topicMapper.selectList(null);

        for (Topic topic : allTopics) {
            Integer likeCount = topicMapper.countLikeByTid(topic.getId());
            Integer collectCount = topicMapper.countCollectByTid(topic.getId());
            int commentCount = topicMapper.countComments(topic.getId());

            if (likeCount == null) likeCount = 0;
            if (collectCount == null) collectCount = 0;

            double score = likeCount + commentCount * 3 + collectCount * 2;

            System.out.println("初始化写入: tid=" + topic.getId() + ", score=" + score);
            stringRedisTemplate.opsForZSet().add(
                "forum:hot:post", topic.getId().toString(), score);
        }
    }
}