package com.printScript.snippetService.DTO;

import java.util.List;

public record TestData(String snippetId, String version, List<String> inputs, List<String> expected) {
}
