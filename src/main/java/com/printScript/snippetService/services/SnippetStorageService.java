package com.printScript.snippetService.services;


import com.printScript.snippetService.DTO.SnippetResponse;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.entities.Test;
import com.printScript.snippetService.errorDTO.SnippetError;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.repositories.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SnippetStorageService {

    @Autowired
    private SnippetRepository snippetRepository;
    @Autowired
    private TestRepository testRepository;

    public SnippetError saveSnippet( String code) {
        try {
            Snippet snippet = new Snippet();
            snippet.setSnippet(code.getBytes());
            snippetRepository.save(snippet);
            return null;
        } catch (Exception e) {
           return new SnippetError(500, "Internal server error");
        }
    }

    public SnippetResponse getSnippet(String snippetId) {
        try {
            Optional<Snippet> snippet = snippetRepository.findById(snippetId);
            if (snippet.isEmpty()) {
                SnippetError error = new SnippetError(404, "Snippet not found");
                return new SnippetResponse(null, error);
            }
            String file = new String(snippet.get().getSnippet());
            return new SnippetResponse(file, null);
        } catch (Exception e) {
            SnippetError error = new SnippetError(500, "Internal server error");
            return new SnippetResponse(null, error);
        }
    }

    public SnippetError deleteSnippet(String snippetId) {
        boolean exists = snippetRepository.existsById(snippetId);
        if (!exists) {
            return new SnippetError(404, "Snippet not found");
        }

        snippetRepository.deleteById(snippetId);
        return null;
    }

    public SnippetError updateSnippet(String snippetId, String snippet) {
        try {
            Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
            if (snippetOptional.isEmpty()) {
                return new SnippetError(404, "Snippet not found");
            }
            Snippet snippetEntity = snippetOptional.get();
            snippetEntity.setSnippet(snippet.getBytes());
            snippetRepository.save(snippetEntity);
            return null;
        } catch (Exception e) {
            return new SnippetError(500, "Internal server error");
        }
    }

    public SnippetError saveTest(String snippetId, String expectedOutput) {
        boolean exists = snippetRepository.existsById(snippetId);
        if (!exists) {
            return new SnippetError(404, "Snippet not found");
        }
        Test test = new Test();
        try {
            test.setExpectedOutput(expectedOutput.getBytes());
            testRepository.save(test);
            return null;
        } catch (Exception e) {
            return new SnippetError(500,  "Internal server error");
        }

    }

    public SnippetError deleteTest(String testId) {
        boolean exists = testRepository.existsById(testId);
        if (!exists) {
            return new SnippetError(404, "Test not found");
        }

        testRepository.deleteById(testId);
        return null;
    }

    public List<String> getTestSuites(String snippetId) {
        boolean exists = snippetRepository.existsById(snippetId);
        if (!exists) {
            return Collections.emptyList();
        }
        return testRepository.findAllExpectedOutputBySnippetId(snippetId);
    }
}
