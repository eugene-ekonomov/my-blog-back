package ru.ekonomov.blogback.model.dto;

import java.util.List;

public record PostsResponse(
        List<PostResponse> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage) {
}
