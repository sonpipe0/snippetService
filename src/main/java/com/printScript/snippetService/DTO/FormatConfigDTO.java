package com.printScript.snippetService.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.printScript.snippetService.entities.FormatConfig;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FormatConfigDTO {
    @NotBlank(message = "User id cannot be blank")
    private String userId;

    @NotNull(message = "Space before colon cannot be null")
    private boolean spaceBeforeColon;

    @NotNull(message = "Space after colon cannot be null")
    private boolean spaceAfterColon;

    @NotNull(message = "Space around equals cannot be null")
    private boolean spaceAroundEquals;

    @NotNull(message = "Lines before println cannot be null")
    private int linesBeforePrintln;

    @NotNull(message = "New line after semicolon cannot be null")
    private boolean newLineAfterSemicolon;

    @NotNull(message = "Enforce spacing between tokens cannot be null")
    private boolean enforceSpacingBetweenTokens;

    @NotNull(message = "Enforce spacing around operators cannot be null")
    private boolean enforceSpacingAroundOperators;

    @NotNull(message = "If brace below line cannot be null")
    private boolean ifBraceBelowLine;

    @NotNull(message = "Indent inside braces cannot be null")
    private int indentInsideBraces;

    public FormatConfig toEntity() {
        FormatConfig formatConfig = new FormatConfig();
        formatConfig.setSpaceBeforeColon(spaceBeforeColon);
        formatConfig.setSpaceAfterColon(spaceAfterColon);
        formatConfig.setSpaceAroundEquals(spaceAroundEquals);
        formatConfig.setLinesBeforePrintln(linesBeforePrintln);
        formatConfig.setNewLineAfterSemicolon(newLineAfterSemicolon);
        formatConfig.setEnforceSpacingBetweenTokens(enforceSpacingBetweenTokens);
        formatConfig.setEnforceSpacingAroundOperators(enforceSpacingAroundOperators);
        formatConfig.setIfBraceBelowLine(ifBraceBelowLine);
        formatConfig.setIndentInsideBraces(indentInsideBraces);
        return formatConfig;
    }

    public FormatConfigDTO defaultConfig(String userId) {
        FormatConfigDTO formatConfigDTO = new FormatConfigDTO();
        formatConfigDTO.setUserId(userId);
        formatConfigDTO.setSpaceBeforeColon(false);
        formatConfigDTO.setSpaceAfterColon(true);
        formatConfigDTO.setSpaceAroundEquals(true);
        formatConfigDTO.setLinesBeforePrintln(1);
        formatConfigDTO.setNewLineAfterSemicolon(false);
        formatConfigDTO.setEnforceSpacingBetweenTokens(true);
        formatConfigDTO.setEnforceSpacingAroundOperators(true);
        formatConfigDTO.setIfBraceBelowLine(true);
        formatConfigDTO.setIndentInsideBraces(4);
        return formatConfigDTO;
    }
}
