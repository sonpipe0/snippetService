package com.printScript.snippetService.errorDTO;

public record Error<B>(int code, B body) {
}
