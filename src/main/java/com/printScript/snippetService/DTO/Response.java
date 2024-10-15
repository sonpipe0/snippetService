package com.printScript.snippetService.DTO;

import com.printScript.snippetService.errorDTO.Error;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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

    public static <T> Response<T> errorFromWebFluxError(WebClientResponseException e) {
        return Response.withError(new Error(e.getStatusCode().value(), e.getResponseBodyAsString()));
    }
}
