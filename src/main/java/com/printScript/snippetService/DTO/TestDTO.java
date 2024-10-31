package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestDTO {

    @NotBlank
    private String testId;

    @NotBlank
    private String testResult;
}
