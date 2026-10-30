package com.box.consumer;

import com.box.config.RabbitMQConfig;
import com.box.constant.NotificationMessage;
import com.box.entity.Notification;
import com.box.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationMapper notificationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationMessage message) {

        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent("notify:dedup:" + message.getMsgId(), "1", 1, TimeUnit.DAYS);
        if (Boolean.FALSE.equals(b)){
            log.info("重复消息，跳过:{}",message.getMsgId());
            return;
        }
        try {
            Notification notification = new Notification();
            BeanUtils.copyProperties(message,notification);
            notification.setTime(LocalDateTime.now());
            notificationMapper.insert(notification);
        }
        catch (Exception e){
            stringRedisTemplate.delete("notify:dedup:" + message.getMsgId());
            log.error("通知入库失败: {}", message.getMsgId(), e);
        }
    }
}
