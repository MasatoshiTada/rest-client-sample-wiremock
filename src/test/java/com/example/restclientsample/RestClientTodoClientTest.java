package com.example.restclientsample;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "todo-service.mock.enabled=false")
public class RestClientTodoClientTest {
  @Autowired
  RestClientTodoClient todoClient;

  static WireMockServer wireMockServer;

  // WireMockの起動を@BeforeEachで行うと、起動のタイミングの問題なのか、一部のテストが失敗する。
  // エクステンションを利用しても同様。
  // そのため、@BeforeAllで起動し、@AfterAllで停止するようにする。
  @BeforeAll
  static void beforeAll() {
    // @WireMockTestを使うと、2つ目以降のNestedクラスがconnection refusedでNGになる。
    // そのため、WireMockServerを直接使う。
    wireMockServer = new WireMockServer(9999);
    wireMockServer.start();
  }

  @AfterAll
  static void afterAll() {
    wireMockServer.stop();
  }

  @Nested
  @DisplayName("getByKeyword()")
  class GetByKeywordTest {
    @Test
    @DisplayName("キーワードを指定すると、該当するTODOのリストを取得できる")
    void success() {
      // WireMockの設定
      wireMockServer.stubFor(get("/api/todos?keyword=a")
          .willReturn(okJson("""
              [
                {
                  "id": 2,
                  "description": "Example 2",
                  "completed": false,
                  "deadline": "2025-10-02T12:00:00",
                  "createdAt": "2025-09-02T12:00:00"
                },
                {
                  "id": 1,
                  "description": "Example 1",
                  "completed": true,
                  "deadline": "2025-10-01T12:00:00",
                  "createdAt": "2025-09-01T12:00:00"
                }
              ]
              """)));
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

    @Test
    @DisplayName("キーワードに該当するTODOがない場合、空のリストを取得できる")
    void empty() {
      // WireMockの設定
      wireMockServer.stubFor(get("/api/todos?keyword=ZZZ")
          .willReturn(okJson("[]")));
      // テストの実行
      List<TodoResponse> actual = todoClient.getByKeyword("ZZZ");
      // 結果の検証
      assertTrue(actual.isEmpty());
    }
  }

  @Nested
  @DisplayName("getById()")
  class GetByIdTest {
    @Test
    @DisplayName("IDを指定すると、該当するTODOのOptionalを取得できる")
    void success() {
      // WireMockの設定
      wireMockServer.stubFor(get("/api/todos/1")
          .willReturn(okJson("""
              {
                "id": 1,
                "description": "Example 1",
                "completed": true,
                "deadline": "2025-10-01T12:00:00",
                "createdAt": "2025-09-01T12:00:00"
              }
              """)));
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

    @Test
    @DisplayName("該当するIDのTODOがない場合は、空のOptionalが返る")
    void empty() {
      // WireMockの設定
      wireMockServer.stubFor(get("/api/todos/999")
          .willReturn(notFound().withBody("""
              {
                "type": "about:blank",
                "status": 404,
                "title": "Not Found",
                "detail": "該当するTODOが見つかりません。",
                "instance": "/api/todos/999",
              }
              """)));
      // テストの実行
      Optional<TodoResponse> actual = todoClient.getById(999);
      // 結果の検証
      assertTrue(actual.isEmpty());
    }
  }

  @Nested
  @DisplayName("register()")
  class RegisterTest {
    @Test
    @DisplayName("TODOを登録すると、Locationレスポンスヘッダーを取得できる")
    void success() {
      // WireMockの設定
      wireMockServer.stubFor(post("/api/todos")
          .withRequestBody(equalToJson("""
              {
                "description": "New Todo",
                "deadline": "2025-10-01T12:00:00"
              }
              """))
          .willReturn(created().withHeader("Location", "/api/todos/4")));
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
      // WireMockの設定
      wireMockServer.stubFor(put("/api/todos/1")
          .withRequestBody(equalToJson("""
              {
                "description": "Updated Todo",
                "completed": true,
                "deadline": "2025-10-01T12:00:00"
              }
              """))
          .willReturn(ok()));
      // リクエストの作成
      TodoRequest request = new TodoRequest(
          "Updated Todo", true, LocalDateTime.parse("2025-10-01T12:00:00"));
      // テストの実行
      todoClient.update(1, request);
      // 結果の検証は特になし（例外が発生しなければ成功）
    }

    @Test
    @DisplayName("存在しないTODOを更新しようとすると、例外が発生する")
    void doesNotExist() {
      // WireMockの設定
      wireMockServer.stubFor(put("/api/todos/999")
          .withRequestBody(equalToJson("""
              {
                "description": "Updated Todo",
                "completed": true,
                "deadline": "2025-10-01T12:00:00"
              }
              """))
          .willReturn(notFound().withBody("""
              {
                "type": "about:blank",
                "status": 404,
                "title": "Not Found",
                "detail": "該当するTODOが見つかりません。",
                "instance": "/api/todos/999"
              }
              """)));
      // リクエストの作成
      TodoRequest request = new TodoRequest(
          "Updated Todo", true, LocalDateTime.parse("2025-10-01T12:00:00"));
      // テストの実行と例外の検証
      assertThrows(TodoNotFoundException.class, () -> todoClient.update(999, request));
    }
  }

  @Nested
  @DisplayName("delete()")
  class DeleteTest {
    @Test
    @DisplayName("TODOを削除すると、何も返らない")
    void success() {
      // WireMockの設定
      wireMockServer.stubFor(delete("/api/todos/1")
          .willReturn(noContent()));
      // テストの実行
      todoClient.delete(1);
      // 結果の検証は特になし（例外が発生しなければ成功）
    }

    @Test
    @DisplayName("存在しないTODOを削除しようとすると、例外が発生する")
    void doesNotExist() {
      // WireMockの設定
      wireMockServer.stubFor(delete("/api/todos/999")
          .willReturn(notFound().withBody("""
              {
                "type": "about:blank",
                "status": 404,
                "title": "Not Found",
                "detail": "該当するTODOが見つかりません。",
                "instance": "/api/todos/999"
              }
              """)));
      // テストの実行と例外の検証
      assertThrows(TodoNotFoundException.class, () -> todoClient.delete(999));
    }
  }
}
