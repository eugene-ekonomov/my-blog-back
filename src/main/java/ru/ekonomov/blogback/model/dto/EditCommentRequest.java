package ru.ekonomov.blogback.model.dto;

public record EditCommentRequest(
        Long id,
        String text,
        Long postId) {
}
