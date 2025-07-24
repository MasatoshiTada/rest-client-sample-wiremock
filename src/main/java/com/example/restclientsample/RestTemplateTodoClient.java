package com.example.restclientsample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "todo-service.mock.enabled", havingValue = "false")
public class RestTemplateTodoClient implements TodoClient {
  private final RestTemplate restTemplate;

  private final String baseUrl;

  public RestTemplateTodoClient(
      RestTemplateBuilder restTemplateBuilder,
      @Value("${todo-service.base-url}") String baseUrl,
      @Value("${todo-service.connect-timeout}") Duration connectTimeout,
      @Value("${todo-service.read-timeout}") Duration readTimeout
  ) {
    this.restTemplate = restTemplateBuilder
        .rootUri(baseUrl)
        .connectTimeout(connectTimeout)
        .readTimeout(readTimeout)
        .build();
    this.baseUrl = baseUrl;
  }

  @Override
  public List<TodoResponse> getByKeyword(String keyword) {
    // exchange()にはrootUriが使われないため、URIは絶対パスで指定
    RequestEntity requestEntity = new RequestEntity<>(
        HttpMethod.GET, URI.create(baseUrl + "/api/todos?keyword=" + keyword));
    ResponseEntity<List<TodoResponse>> responseEntity = restTemplate.exchange(
        requestEntity, new ParameterizedTypeReference<>() {
        }
    );
    List<TodoResponse> todoResponseList = responseEntity.getBody();
    return todoResponseList;
  }

  @Override
  public Optional<TodoResponse> getById(Integer id) {
    try {
      TodoResponse todoResponse = restTemplate.getForObject("/api/todos/" + id, TodoResponse.class);
      return Optional.of(todoResponse);
    } catch (HttpClientErrorException.NotFound e) {
      // 404 Not Found の場合は Optional.empty() を返す
      return Optional.empty();
    }
  }

  @Override
  public URI register(TodoRequest request) {
    URI location = restTemplate.postForLocation("/api/todos", request);
    return location;
  }

  @Override
  public void update(Integer id, TodoRequest request) {
    try {
      restTemplate.put("/api/todos/" + id, request);
    } catch (HttpClientErrorException.NotFound e) {
      // 404 Not Found の場合は例外をスロー
      throw new TodoNotFoundException(e);
    }
  }

  @Override
  public void delete(Integer id) {
    try {
      restTemplate.delete("/api/todos/" + id);
    } catch (HttpClientErrorException.NotFound e) {
      // 404 Not Found の場合は例外をスロー
      throw new TodoNotFoundException(e);
    }
  }
}
