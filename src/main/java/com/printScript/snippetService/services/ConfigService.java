package com.printScript.snippetService.services;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.printScript.snippetService.DTO.FormatConfigDTO;
import com.printScript.snippetService.DTO.LintingConfigDTO;
import com.printScript.snippetService.DTO.Response;
import com.printScript.snippetService.entities.FormatConfig;
import com.printScript.snippetService.entities.LintConfig;
import com.printScript.snippetService.errorDTO.Error;
import com.printScript.snippetService.repositories.FormatConfigRepository;
import com.printScript.snippetService.repositories.LintingConfigRepository;

@Service
public class ConfigService {

    @Autowired
    private LintingConfigRepository lintingConfigRepository;

    @Autowired
    private FormatConfigRepository formatConfigRepository;

    Validator validation = Validation.buildDefaultValidatorFactory().getValidator();

    public Response<Void> putLintingConfig(LintingConfigDTO lintingConfigDTO) {
        Set<ConstraintViolation<LintingConfigDTO>> violations = validation.validate(lintingConfigDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }
        LintConfig lintConfig = lintingConfigDTO.toEntity();
        try {
            lintingConfigRepository.save(lintingConfigDTO.toEntity());
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<LintingConfigDTO> getLintingConfig(String userId) {
        // TODO: Implement this method
        return null;
    }

    public Response<Void> GenerateDefaultLintingConfig(String userId) {
        try {
            LintingConfigDTO lintingConfigDTO = new LintingConfigDTO().defaultConfig(userId);
            lintingConfigRepository.save(lintingConfigDTO.toEntity());
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<Void> putFormatConfig(FormatConfigDTO formatConfigDTO) {
        Set<ConstraintViolation<FormatConfigDTO>> violations = validation.validate(formatConfigDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }
        FormatConfig formatConfig = formatConfigDTO.toEntity();
        try {
            formatConfigRepository.save(formatConfig);
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    public Response<FormatConfigDTO> getFormatConfig(String userId) {
        // TODO: Implement this method
        return null;
    }

    public Response<Void> GenerateDefaultFormatConfig(String userId) {
        try {
            FormatConfigDTO formatConfigDTO = new FormatConfigDTO().defaultConfig(userId);
            formatConfigRepository.save(formatConfigDTO.toEntity());
            return Response.withData(null);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, "Internal Server Error"));
        }
    }

    private <T> Error<?> getViolationsMessageError(Set<ConstraintViolation<T>> violations) {
        StringBuilder message = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            message.append(violation.getMessage()).append("\n");
        }
        return new Error<>(400, message.toString());
    }
}
