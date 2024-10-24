package com.printScript.snippetService.DTO;

import java.io.InputStream;

public record Linting(String code, String version, InputStream config) {
}
