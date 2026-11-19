package ru.ekonomov.blogback.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class Comment {
    private Long id;
    private Long postId;
    private String text;
    private ZonedDateTime createdAt;

    public Comment(Long postId, String text) {
        this(null, postId, text, null);
    }
}
