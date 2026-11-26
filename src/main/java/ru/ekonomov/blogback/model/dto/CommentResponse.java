package ru.ekonomov.blogback.model.dto;

public record CommentResponse(
        Long id,
        String text,
        Long postId) {
}
