package com.box.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

        String tmpkey = "forum:hot:post:tmp";

        //1, 清空临时Key
        stringRedisTemplate.delete(tmpkey);

        //2, 获取7天内的所有帖子
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Topic> allTopics = topicMapper.selectList(
                new LambdaQueryWrapper<Topic>()
                        .ge(Topic::getTime, sevenDaysAgo)
        );

        //3, 遍历帖子，计算分数并写入临时Key
        for (Topic topic : allTopics) {
            Integer likeCount = topicMapper.countLikeByTid(topic.getId());
            Integer collectCount = topicMapper.countCollectByTid(topic.getId());
            int commentCount = topicMapper.countComments(topic.getId());

            if (likeCount == null) likeCount = 0;
            if (collectCount == null) collectCount = 0;

            double base = likeCount + commentCount * 3 + collectCount * 2;

            long hours = Duration.between(topic.getTime(), LocalDateTime.now()).toHours();

            double score = base / Math.pow(hours + 2,1.5);
            stringRedisTemplate.opsForZSet().add(tmpkey, topic.getId().toString(), score);
        }
        // 4. 原子切换（rename 会覆盖旧 key）
        stringRedisTemplate.rename(tmpkey, "forum:hot:post");
    }
}