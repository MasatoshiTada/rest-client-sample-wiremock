package com.example.restclientsample;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

public record TodoRequest(
        String description,
        @JsonInclude(JsonInclude.Include.NON_NULL) Boolean completed,
        LocalDateTime deadline
) {
}
