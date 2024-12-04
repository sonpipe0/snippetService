package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.printScript.snippetService.DTO.*;
import com.printScript.snippetService.entities.Snippet;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.redis.ProducerInterface;
import com.printScript.snippetService.repositories.SnippetRepository;
import com.printScript.snippetService.utils.TokenUtils;
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

    private final ProducerInterface lintProducer;

    private final ProducerInterface formatProducer;

    private final Validator validation = Validation.buildDefaultValidatorFactory().getValidator();

    private final Logger log = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    public SnippetService(ProducerInterface lintProducer, ProducerInterface formatProducer) {
        this.lintProducer = lintProducer;
        this.formatProducer = formatProducer;
    }

    @Transactional
    public Response<SnippetCodeDetails> saveSnippet(SnippetDTO snippetDTO, String token) {
        log.info("saveSnippet was called");
        Set<ConstraintViolation<SnippetDTO>> violations = validation.validate(snippetDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String title = snippetDTO.getTitle();
        String language = snippetDTO.getLanguage();
        String extension = snippetDTO.getExtension();
        String code = snippetDTO.getCode();

        Snippet snippet = new Snippet();
        snippet.setTitle(title);
        snippet.setDescription(snippetDTO.getDescription());
        snippet.setLanguage(language);
        snippet.setExtension(extension);
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
            return Response.withError(permissionsResponse.getError());
        }
        if (language.equals("printscript")) {
            Response<String> printScriptResponse = printScriptServiceHandler.validateCode(code, "1.1", token);
            if (printScriptResponse.isError()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Response.withError(printScriptResponse.getError());
            }
        }

        Response<Void> response = bucketHandler.put("snippets/" + snippetId, code, token);
        if (response.isError())
            return Response.withError(response.getError());

        generateEvents(token, snippetId, snippet, language);

        String author = permissionsManagerHandler.getSnippetAuthor(snippetId, token).getData();

        return Response.withData(new SnippetCodeDetails(author, snippetId, title, snippetDTO.getDescription(), language,
                extension, code, snippet.getLintStatus()));
    }

    public Response<SnippetCodeDetails> updateSnippet(UpdateSnippetDTO updateSnippetDTO, String token) {
        log.info("updateSnippet was called");
        Set<ConstraintViolation<UpdateSnippetDTO>> violations = validation.validate(updateSnippetDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String snippetId = updateSnippetDTO.getSnippetId();
        String title = updateSnippetDTO.getTitle();
        String language = updateSnippetDTO.getLanguage();
        String extension = updateSnippetDTO.getExtension();
        String code = updateSnippetDTO.getCode();

        Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
        if (snippetOptional.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        if (language.equals("printscript")) {
            Response<String> printScriptResponse = printScriptServiceHandler.validateCode(code, "1.1", token);
            if (printScriptResponse.isError())
                return Response.withError(printScriptResponse.getError());
        }
        Snippet snippet = snippetOptional.get();
        snippet.setTitle(title);
        snippet.setDescription(updateSnippetDTO.getDescription());
        snippet.setLanguage(language);
        snippet.setExtension(extension);
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

        String author = permissionsManagerHandler.getSnippetAuthor(snippetId, token).getData();
        String codeSnippet = bucketHandler.get("snippets/" + snippetId, token).getData();

        SnippetCodeDetails snippetDetails = new SnippetCodeDetails();
        snippetDetails.setId(snippetId);
        snippetDetails.setTitle(title);
        snippetDetails.setCode(codeSnippet);
        snippetDetails.setDescription(updateSnippetDTO.getDescription());
        snippetDetails.setLanguage(language);
        snippetDetails.setExtension(extension);
        snippetDetails.setLintStatus(snippet.getLintStatus());
        snippetDetails.setAuthor(author);
        return Response.withData(snippetDetails);
    }

    public Response<SnippetCodeDetails> getSnippetDetails(String snippetId, String token) {
        log.info("getSnippetDetails was called");
        Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
        if (snippetOpt.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Response<String> authorResponse = permissionsManagerHandler.getSnippetAuthor(snippetId, token);
        if (authorResponse.isError())
            return Response.withError(authorResponse.getError());

        Snippet snippet = snippetOpt.get();

        Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
        if (response.isError())
            return Response.withError(response.getError());

        String code = response.getData();
        String extension = snippet.getExtension();
        String language = snippet.getLanguage();

        Snippet.Status lintStatus = snippet.getLintStatus();

        SnippetCodeDetails snippetDetails = new SnippetCodeDetails();
        snippetDetails.setCode(code);
        snippetDetails.setLanguage(language);
        snippetDetails.setAuthor(authorResponse.getData());
        snippetDetails.setExtension(extension);
        snippetDetails.setDescription(snippet.getDescription());
        snippetDetails.setLintStatus(lintStatus);
        snippetDetails.setId(snippetId);
        snippetDetails.setTitle(snippet.getTitle());

        return Response.withData(snippetDetails);
    }

    public Response<String> deleteSnippet(String snippetId, String token) {
        log.info("deleteSnippet was called");
        Response<String> canEditResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (canEditResponse.isError()) {
            Response<String> hasAccessResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                    "/snippets/has-access");
            if (hasAccessResponse.isError()) {
                return Response.withError(hasAccessResponse.getError());
            }

            Response<String> deleteResponse = permissionsManagerHandler.deleteRelation(snippetId,
                    "/snippets/delete/relationship", token);
            if (deleteResponse.isError())
                return Response.withError(deleteResponse.getError());

            return Response.withData(null);
        }

        Response<String> deleteResponse = permissionsManagerHandler.deleteRelation(snippetId,
                "/snippets/delete/all-relationships", token);
        if (deleteResponse.isError())
            return Response.withError(deleteResponse.getError());

        snippetRepository.deleteById(snippetId);

        Response<Void> response = bucketHandler.delete("snippets/" + snippetId, token);
        if (response.isError())
            return Response.withError(response.getError());

        return Response.withData(null);
    }

    public Response<SnippetCodeDetails> shareSnippet(ShareSnippetDTO shareSnippetDTO, String token) {
        log.info("shareSnippet was called");
        Set<ConstraintViolation<ShareSnippetDTO>> violations = validation.validate(shareSnippetDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        Response<String> permissionsResponse = permissionsManagerHandler
                .checkPermissions(shareSnippetDTO.getSnippetId(), token, "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Response<String> permissionsResponse2 = permissionsManagerHandler.shareSnippet(token, shareSnippetDTO,
                "/snippets/save/share/relationship");
        if (permissionsResponse2.isError()) {
            return Response.withError(permissionsResponse2.getError());
        }
        Snippet snippet = snippetRepository.findById(shareSnippetDTO.getSnippetId()).orElse(null);
        if (snippet == null) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }
        Response<String> authorResponse = permissionsManagerHandler.getSnippetAuthor(shareSnippetDTO.getSnippetId(),
                token);

        String code = bucketHandler.get("snippets/" + shareSnippetDTO.getSnippetId(), token).getData();
        SnippetCodeDetails snippetDetails = new SnippetCodeDetails();
        snippetDetails.setId(shareSnippetDTO.getSnippetId());
        snippetDetails.setTitle(snippet.getTitle());
        snippetDetails.setDescription(snippet.getDescription());
        snippetDetails.setLanguage(snippet.getLanguage());
        snippetDetails.setExtension(snippet.getExtension());
        snippetDetails.setLintStatus(snippet.getLintStatus());
        snippetDetails.setAuthor(authorResponse.getData());
        snippetDetails.setCode(code);
        return Response.withData(snippetDetails);
    }

    public record Tuple(String code, String name) {
    }

    public Response<Tuple> downloadSnippet(String snippetId, String token) {
        log.info("downloadSnippet was called");
        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Snippet snippet = snippetRepository.findById(snippetId).orElse(null);
        String extension;
        if (snippet == null)
            return Response.withError(new Error<>(404, "Snippet not found"));
        else {
            extension = snippet.getExtension();
        }

        Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
        if (response.isError())
            return Response.withError(response.getError());

        Tuple tuple = new Tuple(response.getData(), snippet.getTitle().replace(" ", "_") + "." + extension);
        return Response.withData(tuple);
    }

    public Response<String> getFormattedFile(String snippetId, String token) {
        log.info("getFormattedFile was called");
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
        log.info("generateEvents was called");
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

        formatProducer.publishEvent(formatPublishEvent);
    }

    public Response<List<SnippetCodeDetails>> getAccessibleSnippets(String token, String relation, Integer page,
            Integer pageSize, String name) {
        log.info("getAccessibleSnippets was called");
        Response<List<SnippetPermissionGrantResponse>> relationshipsResponse = permissionsManagerHandler
                .getSnippetRelationships(token, relation);
        if (relationshipsResponse.isError()) {
            return Response.withError(relationshipsResponse.getError());
        }

        List<SnippetPermissionGrantResponse> relationships = relationshipsResponse.getData();
        System.out.println(relationships);
        Pageable pageable = PageRequest.of(page, pageSize);
        List<Snippet> snippets = snippetRepository.findByIdInAndTitleStartingWith(
                relationships.stream().map(SnippetPermissionGrantResponse::getSnippetId).toList(), name, pageable);
        System.out.println(snippets);
        List<SnippetCodeDetails> snippetDetails = snippets.stream().map(snippet -> {
            SnippetCodeDetails snippetDetail = new SnippetCodeDetails();
            String code = bucketHandler.get("snippets/" + snippet.getId(), token).getData();
            snippetDetail.setId(snippet.getId());
            snippetDetail.setTitle(snippet.getTitle());
            snippetDetail.setCode(code);
            snippetDetail.setDescription(snippet.getDescription());
            snippetDetail.setLanguage(snippet.getLanguage());
            snippetDetail.setExtension(snippet.getExtension());
            snippetDetail.setLintStatus(snippet.getLintStatus());

            // Find the author from relationships
            String author = relationships.stream().filter(rel -> rel.getSnippetId().equals(snippet.getId()))
                    .map(SnippetPermissionGrantResponse::getAuthor).findFirst().orElse(null);
            snippetDetail.setAuthor(author);

            return snippetDetail;
        }).toList();
        return Response.withData(snippetDetails);
    }

    public Response<PaginatedUsers> getSnippetUsers(String token, String prefix, Integer page, Integer PageSize) {
        log.info("getSnippetUsers was called");
        Response<PaginatedUsers> response = permissionsManagerHandler.getSnippetUsers(token, prefix, page, PageSize);
        if (response.isError()) {
            return Response.withError(response.getError());
        }
        return response;
    }
}
