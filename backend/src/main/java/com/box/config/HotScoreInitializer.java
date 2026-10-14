package com.box.config;

import com.box.entity.Topic;
import com.box.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class HotScoreInitializer implements CommandLineRunner {

    private final TopicMapper topicMapper;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void run(String... args) {
        refreshAll();
    }

    @Scheduled(cron = "0 0 * * * ?")   // 每小时整点
    public void scheduledRefresh() {
        refreshAll();
    }

    private void refreshAll(){
        List<Topic> allTopics = topicMapper.selectList(null);

        for (Topic topic : allTopics) {
            Integer likeCount = topicMapper.countLikeByTid(topic.getId());
            Integer collectCount = topicMapper.countCollectByTid(topic.getId());
            int commentCount = topicMapper.countComments(topic.getId());

            if (likeCount == null) likeCount = 0;
            if (collectCount == null) collectCount = 0;

            double base = likeCount + commentCount * 3 + collectCount * 2;

            long hours = Duration.between(topic.getTime(), LocalDateTime.now()).toHours();

            double score = base / Math.pow(hours + 2,1.5);
            stringRedisTemplate.opsForZSet().add(
                    "forum:hot:post", topic.getId().toString(), score);
        }
    }
}