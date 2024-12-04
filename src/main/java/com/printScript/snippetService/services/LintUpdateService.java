package com.printScript.snippetService.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.redis.ProducerInterface;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.web.ConfigServiceWebHandler;

import events.ConfigPublishEvent;

@Service
public class LintUpdateService {

    private final ProducerInterface lintProducer;

    private static final Logger logger = Logger.getLogger(LintUpdateService.class.getName());

    @Autowired
    private SnippetService snippetService;

    @Autowired
    private ConfigServiceWebHandler configServiceWebHandler;
    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    public LintUpdateService(ProducerInterface lintProducer) {
        this.lintProducer = lintProducer;
    }

    public void sendLintMessages(String userId, String token) {
        List<String> snippets = this.getAllSnippets(userId, token);
        AtomicInteger count = new AtomicInteger();
        for (String snippet : snippets) {
            snippetRepository.findById(snippet).ifPresent(snippetEntity -> {
                snippetEntity.setLintStatus(Snippet.Status.IN_PROGRESS);
                if (!Objects.equals(snippetEntity.getLanguage(), "printscript")) {
                    snippetEntity.setLintStatus(Snippet.Status.UNKNOWN);
                    snippetRepository.save(snippetEntity);
                    logger.warning("Skipping snippet with id: " + snippet + " due to invalid language");
                    return;
                }
                snippetRepository.save(snippetEntity);

                ConfigPublishEvent snippetEvent = new ConfigPublishEvent();
                snippetEvent.setSnippetId(snippet);
                snippetEvent.setUserId(userId);
                snippetEvent.setType(ConfigPublishEvent.ConfigType.LINT);
                try {
                    lintProducer.publishEvent(snippetEvent);
                    count.getAndIncrement();
                } catch (Exception e) {
                    logger.warning("Failed to send lint message for snippet with id: " + snippetEvent.getSnippetId());
                }
            });
        }
    }

    public List<String> getAllSnippets(String userId, String token) {
        Response<List<String>> snippets = configServiceWebHandler.getAllSnippets(userId, token);
        if (snippets.isError()) {
            return new ArrayList<>();
        } else {
            return snippets.getData();
        }
    }
}
