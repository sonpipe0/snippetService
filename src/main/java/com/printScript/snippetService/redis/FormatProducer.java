package com.printScript.snippetService.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import com.printScript.snippetService.config.RedisStreamProducer;

import events.ConfigPublishEvent;

@Component
public class FormatProducer extends RedisStreamProducer implements ProducerInterface {

    private static final Logger logger = LoggerFactory.getLogger(LintProducer.class);

    @Autowired
    public FormatProducer(@Value("${stream.redis.stream.format.key}") String streamKey,
                        ReactiveRedisTemplate<String, String> redis) {
        super(streamKey, redis);
    }

    @Override
    public void publishEvent(ConfigPublishEvent event) {
        logger.info("Publishing event: {}", event.toString());
        emit(event).subscribe();
    }
}
