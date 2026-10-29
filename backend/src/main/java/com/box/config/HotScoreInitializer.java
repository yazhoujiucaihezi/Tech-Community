package com.box.config;

import com.box.constant.RedisConstant;
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


    @Scheduled(cron = "0 */10 * * * ?")  // 每10分钟
    public void scheduledRefresh() {
        refreshAll();
    }

    private void refreshAll(){
        if (stringRedisTemplate.hasKey(RedisConstant.hot_tem)){
            stringRedisTemplate.delete(RedisConstant.hot_tem);
        }
        List<Topic> topics = topicMapper.selectList(null);
        for (Topic topic : topics) {
            if (topic.getTime().plusDays(30).isAfter(LocalDateTime.now())){
                Integer tid = topic.getId();
                long hours = Duration.between(topic.getTime(),LocalDateTime.now()).toHours();
                double count = (topicMapper.countLikeByTid(tid) + topicMapper.countCollectByTid(tid)*2 + topicMapper.countComments(tid) * 3)/Math.pow(hours + 2, 1.8);
                if (count>0) {
                    stringRedisTemplate.opsForZSet().add(RedisConstant.hot_tem, tid.toString(), count);
                }
            }
        }
        if (stringRedisTemplate.hasKey(RedisConstant.hot_tem)) {
            stringRedisTemplate.rename(RedisConstant.hot_tem, RedisConstant.hot);
        }
    }
}
