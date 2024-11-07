package com.printScript.snippetService.redis;

import java.time.Duration;

import org.austral.ingsis.redis.RedisStreamConsumer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Component;

import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.web.BucketRequestExecutor;

import events.ConfigPublishEvent;
import events.StatusPublishEvent;

@Component
public class StatusConsumer extends RedisStreamConsumer<StatusPublishEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StatusConsumer.class);

    @Autowired
    BucketRequestExecutor bucketRequestExecutor;

    @Autowired
    SnippetRepository snippetRepository;

    public StatusConsumer(RedisTemplate<String, String> redis,
            @Value("${stream.redis.stream.status.key}") String streamKey,
            @Value("${stream.redis.consumer.group}") String consumerGroup) {
        super(streamKey, consumerGroup, redis);
    }

    @Override
    protected synchronized void onMessage(@NotNull ObjectRecord<String, StatusPublishEvent> objectRecord) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        StatusPublishEvent event = objectRecord.getValue();
        String snippetId = event.getSnippetId();
        String userId = event.getUserId();
        ConfigPublishEvent.ConfigType type = event.getType();
        StatusPublishEvent.StatusType status = event.getStatus();

        snippetRepository.findById(snippetId).ifPresent(snippet -> {
            switch (type) {
                case LINT :
                    snippet.setLintStatus(translateStatus(status));
                    break;
                case FORMAT :
                    snippet.setFormatStatus(translateStatus(status));
                    break;
            }
            snippetRepository.save(snippet);
        });

        logger.info("Updated snippet with id: " + snippetId + " and user id: " + userId);
    }

    private Snippet.Status translateStatus(StatusPublishEvent.StatusType status) {
        return status == StatusPublishEvent.StatusType.COMPLIANT
                ? Snippet.Status.COMPLIANT
                : Snippet.Status.NON_COMPLIANT;
    }

    @NotNull
    @Override
    protected StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, StatusPublishEvent>> options() {
        return StreamReceiver.StreamReceiverOptions.builder().pollTimeout(Duration.ofSeconds(5))
                .targetType(StatusPublishEvent.class).build();
    }
}
