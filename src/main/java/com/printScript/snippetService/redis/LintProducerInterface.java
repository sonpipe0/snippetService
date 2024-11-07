package com.printScript.snippetService.redis;

import events.ConfigPublishEvent;

public interface LintProducerInterface {
    void publishEvent(ConfigPublishEvent event);
}
