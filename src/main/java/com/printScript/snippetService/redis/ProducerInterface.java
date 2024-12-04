package com.printScript.snippetService.redis;

import events.ConfigPublishEvent;

public interface ProducerInterface {
    void publishEvent(ConfigPublishEvent event);
}
