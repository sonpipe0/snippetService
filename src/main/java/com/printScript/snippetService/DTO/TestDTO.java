package com.printScript.snippetService.DTO;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestDTO {

    @NotBlank(message = "Id is required")
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Input queue is required")
    private List<String> inputQueue;

    @NotNull(message = "Output queue is required")
    private List<String> outputQueue;
}
