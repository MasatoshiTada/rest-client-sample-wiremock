package com.example.restclientsample;

import java.time.LocalDateTime;

public record TodoResponse(
        Integer id,
        String description,
        Boolean completed,
        LocalDateTime deadline,
        LocalDateTime createdAt
) {
}
