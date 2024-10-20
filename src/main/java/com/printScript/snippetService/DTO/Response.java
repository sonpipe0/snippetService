package com.printScript.snippetService.DTO;

import com.printScript.snippetService.errorDTO.Error;

public class Response<T> {
    private T data;
    private Error error;

    public static <T> Response<T> withData(T data) {
        Response<T> response = new Response<>();
        response.data = data;
        return response;
    }

    public static <T> Response<T> withError(Error error) {
        Response<T> response = new Response<>();
        response.error = error;
        return response;
    }

    public T getData() {
        return data;
    }

    public Error getError() {
        return error;
    }

    public boolean isError() {
        return error != null;
    }
}
