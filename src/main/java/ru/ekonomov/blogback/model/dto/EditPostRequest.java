package ru.ekonomov.blogback.model.dto;

import java.util.List;

public record EditPostRequest(
        Long id,
        String title,
        String text,
        List<String> tags) {
}
