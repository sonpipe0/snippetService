package com.printScript.snippetService.services;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.entities.Test;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.repositories.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TestService {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private WebClientService permissionsWebClient;

    @Autowired
    private WebClientService printScriptWebClient;

    @Autowired
    private TestRepository testRepository;

    public Response<String> saveTest(Map<String, Object> postFile) {
        try {
            String snippetId = (String) postFile.get("snippetId");
            String expectedOutput = (String) postFile.get("expectedOutput");

            boolean exists = snippetRepository.existsById(snippetId);
            if (!exists) {
                return Response.withError(new Error(404, "Snippet not found"));
            }

            Test test = new Test();
            test.setExpectedOutput(expectedOutput.getBytes());
            testRepository.save(test);
            String testId = test.getId();

            return Response.withData(testId);
        } catch (Exception e) {
            return Response.withError(new Error(500, "Internal server error"));
        }
    }

    public Response<String> deleteTest(String testId) {
        try {
            boolean exists = testRepository.existsById(testId);
            if (!exists) {
                return Response.withError(new Error(404, "Test not found"));
            }

            testRepository.deleteById(testId);
            return Response.withData(testId);
        } catch (Exception e) {
            return Response.withError(new Error(500, "Internal server error"));
        }
    }

    public Response<List<String>> getTestSuites(String snippetId) {
        try {
            boolean exists = snippetRepository.existsById(snippetId);
            if (!exists) {
                return Response.withError(new Error(404, "Snippet not found"));
            }

            List<String> testSuites = testRepository.findAllExpectedOutputBySnippetId(snippetId);
            return Response.withData(testSuites);
        } catch (Exception e) {
            return Response.withError(new Error(500, "Internal server error"));
        }
    }
}
