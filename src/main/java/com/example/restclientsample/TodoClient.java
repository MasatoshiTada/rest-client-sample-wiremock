package com.example.restclientsample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class TodoClient {
    private final RestClient restClient;

    public TodoClient(
            RestClient.Builder restClientBuilder,
            @Value("${todo-service.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<TodoResponse> getByKeyword(String keyword) {
        List<TodoResponse> todoResponseList = restClient.get()
                .uri("/api/todos?keyword=" + keyword)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return todoResponseList;
    }

    public Optional<TodoResponse> getById(Integer id) {
        try {
            Optional<TodoResponse> todoResponseOptional = restClient.get()
                    .uri("/api/todos/" + id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return todoResponseOptional;
        } catch (HttpClientErrorException.NotFound e) {
            // 404 Not Found の場合は Optional.empty() を返す
            return Optional.empty();
        }
    }

    public URI register(TodoRequest request) {
        ResponseEntity<Void> responseEntity = restClient.post()
                .uri("/api/todos")
                .body(request)
                .retrieve()
                .toBodilessEntity();
        URI location = responseEntity.getHeaders().getLocation();
        return location;
    }

    public void update(Integer id, TodoRequest request) {
        try {
            restClient.put()
                    .uri("/api/todos/" + id)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            // 404 Not Found の場合は例外をスロー
            throw new TodoNotFoundException(e);
        }
    }

    public void delete(Integer id) {
        try {
            restClient.delete()
                    .uri("/api/todos/" + id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            // 404 Not Found の場合は例外をスロー
            throw new TodoNotFoundException(e);
        }
    }

    public void patch(Integer id) {
        try {
            restClient.patch()
                    .uri("/api/todos/" + id + "/done")
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            // 404 Not Found の場合は例外をスロー
            throw new TodoNotFoundException(e);
        }
    }
}
