package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.LintProducerInterface;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.utils.TokenUtils;
import com.printScript.snippetService.web.ConfigServiceWebHandler;
import com.printScript.snippetService.web.handlers.BucketHandler;
import com.printScript.snippetService.web.handlers.PermissionsManagerHandler;
import com.printScript.snippetService.web.handlers.PrintScriptServiceHandler;

import events.ConfigPublishEvent;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@Service
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private BucketHandler bucketHandler;

    @Autowired
    private PermissionsManagerHandler permissionsManagerHandler;

    @Autowired
    private PrintScriptServiceHandler printScriptServiceHandler;

    private final LintProducerInterface lintProducer;

    private final Validator validation = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Logger logger = LoggerFactory.getLogger(SnippetService.class);
    @Autowired
    private ConfigServiceWebHandler configServiceWebHandler;

    @Autowired
    public SnippetService(LintProducerInterface lintProducer) {
        this.lintProducer = lintProducer;
    }

    @Transactional
    public Response<String> saveSnippet(SnippetDTO snippetDTO, String token) {
        Set<ConstraintViolation<SnippetDTO>> violations = validation.validate(snippetDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String title = snippetDTO.getTitle();
        String language = snippetDTO.getLanguage();
        String version = snippetDTO.getVersion();
        String code = snippetDTO.getCode();

        Snippet snippet = new Snippet();
        snippet.setTitle(title);
        snippet.setDescription(snippetDTO.getDescription());
        snippet.setLanguage(language);
        snippet.setVersion(version);
        snippet.setLintStatus(Snippet.Status.IN_PROGRESS);
        snippet.setFormatStatus(Snippet.Status.IN_PROGRESS);

        try {
            snippetRepository.save(snippet);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        String snippetId = snippet.getId();

        Response<String> permissionsResponse = permissionsManagerHandler.saveRelation(token, snippetId,
                "/snippets/save/relationship");
        if (permissionsResponse.isError()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return permissionsResponse;
        }
        if (language.equals("printscript")) {
            Response<String> printScriptResponse = printScriptServiceHandler.validateCode(code, version, token);
            if (printScriptResponse.isError()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return printScriptResponse;
            }
        }

        Response<Void> response = bucketHandler.put("snippets/" + snippetId, code, token);
        if (response.isError())
            return Response.withError(response.getError());

        generateEvents(token, snippetId, snippet, language);

        return Response.withData(snippetId);
    }

    public Response<String> updateSnippet(UpdateSnippetDTO updateSnippetDTO, String token) {
        Set<ConstraintViolation<UpdateSnippetDTO>> violations = validation.validate(updateSnippetDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String snippetId = updateSnippetDTO.getSnippetId();
        String title = updateSnippetDTO.getTitle();
        String language = updateSnippetDTO.getLanguage();
        String version = updateSnippetDTO.getVersion();
        String code = updateSnippetDTO.getCode();

        Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
        if (snippetOptional.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (permissionsResponse.isError())
            return permissionsResponse;

        if (language.equals("printscript")) {
            Response<String> printScriptResponse = printScriptServiceHandler.validateCode(code, version, token);
            if (printScriptResponse.isError())
                return printScriptResponse;
        }
        Snippet snippet = snippetOptional.get();
        snippet.setTitle(title);
        snippet.setDescription(updateSnippetDTO.getDescription());
        snippet.setLanguage(language);
        snippet.setVersion(version);
        snippet.setLintStatus(Snippet.Status.IN_PROGRESS);
        snippet.setFormatStatus(Snippet.Status.IN_PROGRESS);

        try {
            snippetRepository.save(snippet);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        bucketHandler.put("snippets/" + snippetId, code, token);

        generateEvents(token, snippetId, snippet, language);
        snippetRepository.save(snippet);
        return Response.withData("Snippet updated successfully");
    }

    public Response<SnippetCodeDetails> getSnippetDetails(String snippetId, String token) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
        if (snippetOpt.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Snippet snippet = snippetOpt.get();

        Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
        if (response.isError())
            return Response.withError(response.getError());

        String code = response.getData();
        String version = snippet.getVersion();
        String language = snippet.getLanguage();

        Snippet.Status lintStatus = snippet.getLintStatus();

        SnippetCodeDetails snippetDetails = new SnippetCodeDetails();
        snippetDetails.setCode(code);
        snippetDetails.setLanguage(language);
        snippetDetails.setVersion(version);
        snippetDetails.setDescription(snippet.getDescription());
        snippetDetails.setLintStatus(lintStatus);
        snippetDetails.setId(snippetId);
        snippetDetails.setTitle(snippet.getTitle());

        return Response.withData(snippetDetails);
    }

    public Response<String> shareSnippet(ShareSnippetDTO shareSnippetDTO, String token) {
        Set<ConstraintViolation<ShareSnippetDTO>> violations = validation.validate(shareSnippetDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        Response<String> permissionsResponse = permissionsManagerHandler
                .checkPermissions(shareSnippetDTO.getSnippetId(), token, "/snippets/has-access");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Response<String> permissionsResponse2 = permissionsManagerHandler.shareSnippet(token, shareSnippetDTO,
                "/snippets/save/share/relationship");
        if (permissionsResponse2.isError()) {
            return permissionsResponse2;
        }

        return Response.withData("Snippet shared successfully");
    }

    public record Tuple(String code, String name) {
    }

    public Response<Tuple> downloadSnippet(String snippetId, String token) {
        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Snippet snippet = snippetRepository.findById(snippetId).orElse(null);
        String language;
        if (snippet == null)
            return Response.withError(new Error<>(404, "Snippet not found"));
        else {
            language = snippet.getLanguage();
        }

        Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
        if (response.isError())
            return Response.withError(response.getError());

        Tuple tuple = new Tuple(response.getData(), snippet.getTitle().replace(" ", "_") + "." + language);
        return Response.withData(tuple);
    }

    public Response<String> getFormattedFile(String snippetId, String token) {
        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
        if (snippetOptional.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Snippet snippet = snippetOptional.get();
        if (snippet.getFormatStatus() == Snippet.Status.IN_PROGRESS) {
            return Response.withError(new Error<>(400, "Format is in progress"));
        }

        Response<String> response;
        try {
            response = bucketHandler.get("formatted/" + snippetId, token);
            if (response.isError()) {
                return response;
            }
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
        return Response.withData(response.getData());
    }

    private void generateEvents(String token, String snippetId, Snippet snippet, String language) {
        if (!language.equals("printscript")) {
            snippet.setFormatStatus(Snippet.Status.UNKNOWN);
            snippet.setLintStatus(Snippet.Status.UNKNOWN);
            snippetRepository.save(snippet);
            return;
        }
        ConfigPublishEvent lintPublishEvent = new ConfigPublishEvent();
        String userId = TokenUtils.decodeToken(token.substring(7)).get("userId");
        lintPublishEvent.setSnippetId(snippetId);
        lintPublishEvent.setUserId(userId);
        lintPublishEvent.setType(ConfigPublishEvent.ConfigType.LINT);

        lintProducer.publishEvent(lintPublishEvent);

        snippet.setLintStatus(Snippet.Status.IN_PROGRESS);
        snippetRepository.save(snippet);

        ConfigPublishEvent formatPublishEvent = new ConfigPublishEvent();
        formatPublishEvent.setSnippetId(snippetId);
        formatPublishEvent.setUserId(userId);
        formatPublishEvent.setType(ConfigPublishEvent.ConfigType.FORMAT);

        lintProducer.publishEvent(formatPublishEvent);
    }

    public Response<List<SnippetDetails>> getAccessibleSnippets(String token, String relation,
            Snippet.Status lintStatus) {
        Response<List<String>> relationshipsResponse = permissionsManagerHandler.getSnippetRelationships(token,
                relation);
        if (relationshipsResponse.isError()) {
            return Response.withError(relationshipsResponse.getError());
        }

        List<String> relationships = relationshipsResponse.getData();
        List<Snippet> snippets = lintStatus != null
                ? snippetRepository.findByIdIn(relationships).stream().filter(snippet -> {
                    return snippet.getLintStatus() == lintStatus;
                }).toList()
                : snippetRepository.findByIdIn(relationships);
        List<SnippetDetails> snippetDetails = snippets.stream().map(snippet -> {
            return new SnippetDetails(snippet.getId(), snippet.getTitle(), snippet.getDescription(),
                    snippet.getLanguage(), snippet.getVersion(), snippet.getLintStatus());
        }).toList();
        return Response.withData(snippetDetails);
    }
}
