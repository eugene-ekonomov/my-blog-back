package ru.ekonomov.blogback.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Post {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private int likesCount;
    private int commentsCount;
    private ZonedDateTime createdAt;

    public Post(String title, String text, List<String> tags) {
        this(null, title, text, tags, 0, 0, null);
    }
}
