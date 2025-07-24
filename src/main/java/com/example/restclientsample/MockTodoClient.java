package com.example.restclientsample;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ローカルなどで動かすためのモックのTodoClient実装クラス。
 * 各メソッドは固定値を返します。
 */
@Component
@ConditionalOnProperty(name = "todo-service.mock.enabled", havingValue = "true")
public class MockTodoClient implements TodoClient {
  @Override
  public List<TodoResponse> getByKeyword(String keyword) {
    return List.of(
        new TodoResponse(
            2,
            "Example 2",
            false,
            LocalDateTime.parse("2025-10-02T12:00:00"),
            LocalDateTime.parse("2025-09-02T12:00:00")
        ),
        new TodoResponse(
            1,
            "Example 1",
            true,
            LocalDateTime.parse("2025-10-01T12:00:00"),
            LocalDateTime.parse("2025-09-01T12:00:00")
        )
    );
  }

  @Override
  public Optional<TodoResponse> getById(Integer id) {
    return Optional.of(new TodoResponse(
        1,
        "Example 1",
        true,
        LocalDateTime.parse("2025-10-01T12:00:00"),
        LocalDateTime.parse("2025-09-01T12:00:00")
    ));
  }

  @Override
  public URI register(TodoRequest request) {
    return URI.create("/api/todos/4");
  }

  @Override
  public void update(Integer id, TodoRequest request) {
    // 何もしない
  }

  @Override
  public void delete(Integer id) {
    // 何もしない
  }
}
