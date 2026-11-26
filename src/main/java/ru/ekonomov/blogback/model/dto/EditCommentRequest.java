package ru.ekonomov.blogback.model.dto;

import jakarta.validation.constraints.NotBlank;

public record EditCommentRequest(
        Long id,
        @NotBlank String text,
        Long postId) {
}
