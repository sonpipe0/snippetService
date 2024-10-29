package com.printScript.snippetService.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestDTO {
    private String testId; // Cambia el nombre de este campo según tu entidad Test
    private String testResult; // Cambia el nombre de este campo según lo que necesites mostrar
}
