package com.example.restclientsample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "todo-service.mock.enabled", havingValue = "false")
public class RestClientTodoClient implements TodoClient {
  private final RestClient restClient;

  public RestClientTodoClient(
      RestClient.Builder restClientBuilder,
      @Value("${todo-service.base-url}") String baseUrl,
      @Value("${todo-service.connect-timeout}") Duration connectTimeout,
      @Value("${todo-service.read-timeout}") Duration readTimeout
  ) {
    HttpComponentsClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.httpComponents()
        .withCustomizer(factory -> {
          factory.setConnectTimeout(connectTimeout);
          factory.setReadTimeout(readTimeout);
        }).build();
    this.restClient = restClientBuilder
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .build();
  }

  @Override
  public List<TodoResponse> getByKeyword(String keyword) {
    List<TodoResponse> todoResponseList = restClient.get()
        .uri("/api/todos?keyword=" + keyword)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });
    return todoResponseList;
  }

  @Override
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

  @Override
  public URI register(TodoRequest request) {
    ResponseEntity<Void> responseEntity = restClient.post()
        .uri("/api/todos")
        .body(request)
        .retrieve()
        .toBodilessEntity();
    URI location = responseEntity.getHeaders().getLocation();
    return location;
  }

  @Override
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

  @Override
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
}
