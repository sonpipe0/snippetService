package com.printScript.snippetService.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import com.printScript.snippetService.config.RedisStreamProducer;

@Component
public class LintProducer extends RedisStreamProducer implements LintProducerInterface {

    @Autowired
    public LintProducer(@Value("${stream.lint.key}") String streamKey, ReactiveRedisTemplate<String, String> redis) {
        super(streamKey, redis);
    }

    @Override
    public void publishEvent(String name) {
        System.out.println("Publishing on stream: " + getStreamKey());
        emit(name).subscribe();
    }
}
