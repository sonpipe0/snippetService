package com.printScript.snippetService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.printScript.snippetService.redis.LintProducerInterface;

@Primary
@Component
public class SpyLintProducer implements LintProducerInterface {

    private final ArrayList<String> seen = new ArrayList<>();

    @Override
    public void publishEvent(String name) {
        seen.add(name);
    }

    public List<String> events() {
        return Collections.unmodifiableList(seen);
    }

    public void reset() {
        seen.clear();
    }
}
