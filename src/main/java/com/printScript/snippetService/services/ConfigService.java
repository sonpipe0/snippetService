package com.printScript.snippetService.services;

import static com.printScript.snippetService.utils.Utils.getViolationsMessageError;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.entities.FormatConfig;
import com.printScript.snippetService.entities.LintConfig;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.repositories.FormatConfigRepository;
import com.printScript.snippetService.repositories.LintingConfigRepository;
import com.printScript.snippetService.web.BucketRequestExecutor;

import DTO.FormatConfigDTO;
import DTO.LintingConfigDTO;
import Utils.FormatSerializer;
import Utils.LintSerializer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@Service
public class ConfigService {

    @Autowired
    private BucketRequestExecutor bucketRequestExecutor;

    @Autowired
    private LintingConfigRepository lintingConfigRepository;

    @Autowired
    private FormatConfigRepository formatConfigRepository;

    private final Validator validation = Validation.buildDefaultValidatorFactory().getValidator();

    public Response<Void> putLintingConfig(LintingConfigDTO lintingConfigDTO, String userId, String token)
            throws IOException {
        Set<ConstraintViolation<LintingConfigDTO>> violations = validation.validate(lintingConfigDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }
        String lintJson = new LintSerializer().serialize(lintingConfigDTO);
        try {
            bucketRequestExecutor.put("lint/" + userId, lintJson, token);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
        LintConfig lintConfig = new LintConfig();
        lintConfig.setId(userId);
        lintConfig.setLanguage("printScript");
        lintConfig.setVersion("1.1");
        try {
            lintingConfigRepository.save(lintConfig);
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<LintingConfigDTO> getLintingConfig(String userId, String token) {
        try {
            Optional<LintConfig> lintConfig = lintingConfigRepository.findById(userId);
            if (lintConfig.isEmpty()) {
                return Response.withError(new Error<>(404, "Not Found"));
            }
            Response<String> response = bucketRequestExecutor.get("lint/" + userId, token);
            if (response.getError() != null) {
                return Response.withError(response.getError());
            }
            LintingConfigDTO lintingConfigDTO = new LintSerializer().deserialize(response.getData());
            return Response.withData(lintingConfigDTO);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<Void> generateDefaultLintingConfig(String userId, String token) {
        try {
            LintingConfigDTO lintingConfigDTO = new LintingConfigDTO();
            lintingConfigDTO.setIdentifierFormat(LintingConfigDTO.IdentifierFormat.CAMEL_CASE);
            lintingConfigDTO.setRestrictPrintln(true);
            lintingConfigDTO.setRestrictReadInput(false);
            LintConfig lintConfig = new LintConfig();
            lintConfig.setId(userId);
            lintConfig.setLanguage("printScript");
            lintConfig.setVersion("1.1");
            lintingConfigRepository.save(lintConfig);
            String lintJson = new LintSerializer().serialize(lintingConfigDTO);
            bucketRequestExecutor.put("lint/" + userId, lintJson, token);
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<Void> putFormatConfig(FormatConfigDTO formatConfigDTO, String userId, String token)
            throws IOException {
        Set<ConstraintViolation<FormatConfigDTO>> violations = validation.validate(formatConfigDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }
        String formatJson = new FormatSerializer().serialize(formatConfigDTO);
        try {
            bucketRequestExecutor.put("format/" + userId, formatJson, token);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
        FormatConfig formatConfig = new FormatConfig();
        formatConfig.setId(userId);
        formatConfig.setLanguage("printScript");
        formatConfig.setVersion("1.1");
        try {
            formatConfigRepository.save(formatConfig);
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<FormatConfigDTO> getFormatConfig(String userId, String token) {
        try {
            Optional<FormatConfig> formatConfig = formatConfigRepository.findById(userId);
            if (formatConfig.isEmpty()) {
                return Response.withError(new Error<>(404, "Not Found"));
            }
            Response<String> response = bucketRequestExecutor.get("format/" + userId, token);
            if (response.getError() != null) {
                return Response.withError(response.getError());
            }
            FormatConfigDTO formatConfigDTO = new FormatSerializer().deserialize(response.getData());
            return Response.withData(formatConfigDTO);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<Void> generateDefaultFormatConfig(String userId, String token) {
        try {
            FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
            formatConfigDTO.setIndentInsideBraces(4);
            formatConfigDTO.setIfBraceBelowLine(false);
            formatConfigDTO.setSpaceAfterColon(true);
            formatConfigDTO.setSpaceBeforeColon(true);
            formatConfigDTO.setEnforceSpacingAroundOperators(true);
            formatConfigDTO.setNewLineAfterSemicolon(true);
            formatConfigDTO.setSpaceAroundEquals(true);
            formatConfigDTO.setEnforceSpacingBetweenTokens(false);
            formatConfigDTO.setLinesBeforePrintln(0);

            FormatConfig formatConfig = new FormatConfig();
            formatConfig.setId(userId);
            formatConfig.setLanguage("printScript");
            formatConfig.setVersion("1.1");
            formatConfigRepository.save(formatConfig);
            String formatJson = new FormatSerializer().serialize(formatConfigDTO);
            bucketRequestExecutor.put("format/" + userId, formatJson, token);
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }
}
