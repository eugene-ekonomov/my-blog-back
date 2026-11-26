package ru.ekonomov.blogback.repository;

import ru.ekonomov.blogback.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    List<Comment> findAllByPostId(long postId);

    Optional<Comment> findById(long postId, long id);

    Comment create(long postId, Comment comment);

    Optional<Comment> update(long postId, long id, Comment comment);

    int deleteById(long postId, long id);

}
