package com.printScript.snippetService.DTO;

import javax.validation.constraints.NotNull;

import com.printScript.snippetService.entities.LintConfig;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class LintingConfigDTO {
    @NotBlank(message = "User id cannot be blank")
    private String userId;

    @NotNull(message = "Restrict println cannot be null")
    private boolean restrictPrintln;

    @NotNull(message = "Restrict read input cannot be null")
    private boolean restrictReadInput;

    public LintingConfigDTO(String userId, boolean restrictPrintln, boolean restrictReadInput) {
        this.userId = userId;
        this.restrictPrintln = restrictPrintln;
        this.restrictReadInput = restrictReadInput;
    }

    public LintConfig toEntity() {
        LintConfig lintConfig = new LintConfig();
        lintConfig.setRestrictPrintln(restrictPrintln);
        lintConfig.setRestrictReadInput(restrictReadInput);
        return lintConfig;
    }

    public LintingConfigDTO defaultConfig(String userId) {
        return new LintingConfigDTO(userId, false, false);
    }
}
