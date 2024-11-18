package com.printScript.snippetService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.printScript.snippetService.redis.LintProducerInterface;

import events.ConfigPublishEvent;

@Component
public class SpyLintProducer implements LintProducerInterface {

    private final ArrayList<ConfigPublishEvent> seen = new ArrayList<>();

    @Override
    public void publishEvent(ConfigPublishEvent name) {
        seen.add(name);
    }

    public List<ConfigPublishEvent> events() {
        return Collections.unmodifiableList(seen);
    }

    public void reset() {
        seen.clear();
    }
}
