package com.example.restclientsample;

import org.springframework.web.client.HttpClientErrorException;

public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(HttpClientErrorException.NotFound cause) {
        super("TODOが存在しません。", cause);
    }
}
