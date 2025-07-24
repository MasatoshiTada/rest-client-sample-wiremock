package com.example.restclientsample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "todo-service.mock.enabled=true")
public class MockTodoClientTest {
  @Autowired
  MockTodoClient todoClient;

  @Nested
  @DisplayName("getByKeyword()")
  class GetByKeywordTest {
    @Test
    @DisplayName("キーワードを指定すると、該当するTODOのリストを取得できる")
    void success() {
      // テストの実行
      List<TodoResponse> actual = todoClient.getByKeyword("a");
      // 結果の検証
      assertEquals(
          List.of(
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
          ), actual
      );
    }
  }

  @Nested
  @DisplayName("getById()")
  class GetByIdTest {
    @Test
    @DisplayName("IDを指定すると、該当するTODOのOptionalを取得できる")
    void success() {
      // テストの実行
      Optional<TodoResponse> actual = todoClient.getById(1);
      // 結果の検証
      assertEquals(
          new TodoResponse(
              1,
              "Example 1",
              true,
              LocalDateTime.parse("2025-10-01T12:00:00"),
              LocalDateTime.parse("2025-09-01T12:00:00")
          ), actual.get()
      );
    }
  }

  @Nested
  @DisplayName("register()")
  class RegisterTest {
    @Test
    @DisplayName("TODOを登録すると、Locationレスポンスヘッダーを取得できる")
    void success() {
      // リクエストの作成
      TodoRequest request = new TodoRequest("New Todo", null, LocalDateTime.parse("2025-10-01T12:00:00"));
      // テストの実行
      URI actual = todoClient.register(request);
      // 結果の検証
      assertEquals(URI.create("/api/todos/4"), actual);
    }
  }

  @Nested
  @DisplayName("update()")
  class UpdateTest {
    @Test
    @DisplayName("TODOを更新すると、何も返らない")
    void success() {
      // リクエストの作成
      TodoRequest request = new TodoRequest(
          "Updated Todo", true, LocalDateTime.parse("2025-10-01T12:00:00"));
      // テストの実行
      todoClient.update(1, request);
      // 結果の検証は特になし（例外が発生しなければ成功）
    }
  }

  @Nested
  @DisplayName("delete()")
  class DeleteTest {
    @Test
    @DisplayName("TODOを削除すると、何も返らない")
    void success() {
      // テストの実行
      todoClient.delete(1);
      // 結果の検証は特になし（例外が発生しなければ成功）
    }
  }
}
