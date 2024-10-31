package com.printScript.snippetService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestDTO {

    @NotBlank(message = "Test Id is required")
    private String testId;

    @NotBlank(message = "Rest result is required")
    private String testResult;
}
