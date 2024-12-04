package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.getViolationsMessageError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(TestService.class);
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
        log.info("createTest was called");
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
        if (snippet.get().getTests().stream().map(Test::getTitle).toList().contains(testDTO.getTitle())) {
            test = snippet.get().getTests().stream().filter(t -> t.getTitle().equals(testDTO.getTitle())).findFirst()
                    .get();
        }

        test.setTitle(testDTO.getTitle());
        test.setSnippet(snippet.get());
        test.setInputs(
                testDTO.getInputQueue().stream().map(input -> "(" + input + ")").collect(Collectors.joining("(*)")));
        test.setExpectedOutputs(
                testDTO.getOutputQueue().stream().map(output -> "(" + output + ")").collect(Collectors.joining("(*)")));

        try {
            testRepository.save(test);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        return Response.withData(test.getId());
    }

    public Response<List<TestDTO>> getTestsForSnippet(String snippetId, String token) {
        log.info("getTestsForSnippet was called");
        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        List<Test> tests = testRepository.findBySnippetId(snippetId);

        List<TestDTO> testDTOs = tests.stream().map(test -> {
            TestDTO testDTO = new TestDTO();
            testDTO.setId(test.getId());
            testDTO.setTitle(test.getTitle());
            testDTO.setInputQueue(splitTopLevel(test.getInputs(), "("));
            testDTO.setOutputQueue(splitTopLevel(test.getExpectedOutputs(), "("));
            if (test.getInputs().equals(""))
                testDTO.setInputQueue(List.of());
            if (test.getExpectedOutputs().equals(""))
                testDTO.setOutputQueue(List.of());
            return testDTO;
        }).collect(Collectors.toList());

        return Response.withData(testDTOs);
    }

    private List<String> splitTopLevel(String str, String delimiter) {
        log.info("splitTopLevel was called");
        List<String> result = new ArrayList<>();
        int level = 0;
        if (str.isEmpty())
            return List.of();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '(')
                level++;
            if (c == ')')
                level--;
            if (level == 0 && str.startsWith(delimiter, i)) {
                result.add(sb.toString());
                sb.setLength(0);
                i += delimiter.length() - 1;
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.stream().map(s -> s.substring(1, s.length() - 1)).collect(Collectors.toList()); // Remove outer
        // brackets
    }

    public Response<Void> updateTest(TestDTO testDTO, String token) {
        log.info("updateTest was called");
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
        test.get().setInputs(
                testDTO.getInputQueue().stream().map(input -> "(" + input + ")").collect(Collectors.joining("(*)")));
        test.get().setExpectedOutputs(
                testDTO.getOutputQueue().stream().map(output -> "(" + output + ")").collect(Collectors.joining("(*)")));

        try {
            testRepository.save(test.get());
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        return Response.withData(null);
    }

    public Response<Void> deleteTest(String testId, String token) {
        log.info("deleteTest was called");
        Optional<Test> test = testRepository.findById(testId);
        if (test.isEmpty())
            return Response.withError(new Error<>(404, "Test not found"));

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

    public Response<Void> runTest(TestDTO testDTO, String token) {
        log.info("runTest was called");
        Optional<Snippet> snippet = snippetRepository.findById(testDTO.getId());
        if (snippet.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }
        String snippetId = snippet.get().getId();

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        log.info("testDTO: {}, {}, {}, {}", testDTO.getId(), testDTO.getTitle(), testDTO.getInputQueue(),
                testDTO.getOutputQueue());
        Response<Void> printScriptResponse = printScriptServiceHandler.executeTest(snippetId, "1.1",
                testDTO.getInputQueue(), testDTO.getOutputQueue(), token);
        if (printScriptResponse.isError())
            return Response.withError(printScriptResponse.getError());

        return Response.withData(null);
    }
}
