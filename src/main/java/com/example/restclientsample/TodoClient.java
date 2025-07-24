package com.example.restclientsample;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface TodoClient {
  List<TodoResponse> getByKeyword(String keyword);

  Optional<TodoResponse> getById(Integer id);

  URI register(TodoRequest request);

  void update(Integer id, TodoRequest request);

  void delete(Integer id);
}
