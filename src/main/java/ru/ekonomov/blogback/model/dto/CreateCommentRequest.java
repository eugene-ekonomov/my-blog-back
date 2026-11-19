package ru.ekonomov.blogback.model.dto;

public record CreateCommentRequest(
        String text,
        Long postId) {
}
