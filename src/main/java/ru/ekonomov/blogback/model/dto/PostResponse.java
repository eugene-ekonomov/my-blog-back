package ru.ekonomov.blogback.model.dto;

import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String text,
        List<String> tags,
        int likesCount,
        int commentsCount) {
}
