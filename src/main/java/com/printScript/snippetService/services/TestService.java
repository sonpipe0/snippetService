package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.getViolationsMessageError;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.DTO.TestDTO;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.entities.Test;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.repositories.TestRepository;
import com.printScript.snippetService.web.handlers.BucketHandler;
import com.printScript.snippetService.web.handlers.PermissionsManagerHandler;
import com.printScript.snippetService.web.handlers.PrintScriptServiceHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private BucketHandler bucketHandler;

    @Autowired
    private PermissionsManagerHandler permissionsManagerHandler;

    @Autowired
    private PrintScriptServiceHandler printScriptServiceHandler;

    private final Validator validation = Validation.buildDefaultValidatorFactory().getValidator();

    public Response<String> createTest(TestDTO testDTO, String token) {
        Set<ConstraintViolation<TestDTO>> violations = validation.validate(testDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String snippetId = testDTO.getId();

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Optional<Snippet> snippet = snippetRepository.findById(snippetId);
        if (snippet.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Test test = new Test();
        test.setTitle(testDTO.getTitle());
        test.setSnippet(snippet.get());
        test.setInputs(String.join(",", testDTO.getInputQueue()));
        test.setExpectedOutputs(String.join(",", testDTO.getOutputQueue()));

        try {
            testRepository.save(test);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        return Response.withData(test.getId());
    }

    public Response<List<TestDTO>> getTestsForSnippet(String snippetId, String token) {
        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        List<Test> tests = testRepository.findBySnippetId(snippetId);

        List<TestDTO> testDTOs = tests.stream().map(test -> {
            TestDTO testDTO = new TestDTO();
            testDTO.setId(test.getId());
            testDTO.setTitle(test.getTitle());
            testDTO.setInputQueue(List.of(test.getInputs().split(",")));
            testDTO.setOutputQueue(List.of(test.getExpectedOutputs().split(",")));
            return testDTO;
        }).collect(Collectors.toList());

        return Response.withData(testDTOs);
    }

    public Response<Void> updateTest(TestDTO testDTO, String token) {
        Set<ConstraintViolation<TestDTO>> violations = validation.validate(testDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String testId = testDTO.getId();

        Optional<Test> test = testRepository.findById(testId);
        if (test.isEmpty()) {
            return Response.withError(new Error<>(404, "Test not found"));
        }

        Snippet snippet = test.get().getSnippet();
        String snippetId = snippet.getId();

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        test.get().setTitle(testDTO.getTitle());
        test.get().setInputs(String.join(",", testDTO.getInputQueue()));
        test.get().setExpectedOutputs(String.join(",", testDTO.getOutputQueue()));

        try {
            testRepository.save(test.get());
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        return Response.withData(null);
    }

    public Response<Void> deleteTest(String testId, String token) {
        Optional<Test> test = testRepository.findById(testId);
        if (test.isEmpty()) {
            return Response.withError(new Error<>(404, "Test not found"));
        }

        Snippet snippet = test.get().getSnippet();
        String snippetId = snippet.getId();

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        try {
            testRepository.delete(test.get());
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        return Response.withData(null);
    }

    public Response<Void> runTest(String testId, String token) {
        Optional<Test> test = testRepository.findById(testId);
        if (test.isEmpty()) {
            return Response.withError(new Error<>(404, "Test not found"));
        }

        Snippet snippet = test.get().getSnippet();
        String snippetId = snippet.getId();

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        String version = snippet.getVersion();
        List<String> inputs = List.of(test.get().getInputs().split(","));
        List<String> expectedOutputs = List.of(test.get().getExpectedOutputs().split(","));

        Response<Void> printScriptResponse = printScriptServiceHandler.executeTest(snippetId, version, inputs,
                expectedOutputs, token);
        if (printScriptResponse.isError())
            return Response.withError(printScriptResponse.getError());

        return Response.withData(null);
    }
}
