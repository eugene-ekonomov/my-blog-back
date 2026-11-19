package ru.ekonomov.blogback.model.dto;

import java.util.List;

public record CreatePostRequest(String title,
                                String text,
                                List<String> tags) {
}
