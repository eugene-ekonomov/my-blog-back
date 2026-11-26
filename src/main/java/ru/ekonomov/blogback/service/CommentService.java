package ru.ekonomov.blogback.service;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.ekonomov.blogback.model.Comment;
import ru.ekonomov.blogback.model.dto.CommentResponse;
import ru.ekonomov.blogback.model.dto.CreateCommentRequest;
import ru.ekonomov.blogback.model.dto.EditCommentRequest;
import ru.ekonomov.blogback.repository.CommentRepository;

import java.sql.SQLException;
import java.util.List;

@Service
public class CommentService {
    private static final String FOREIGN_KEY_VIOLATION_SQL_STATE = "23503";

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentResponse> findAllByPostId(long postId) {
        return commentRepository.findAllByPostId(postId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CommentResponse findById(long postId, long id) {
        return commentRepository.findById(postId, id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment " + id + " is not found"));
    }

    public CommentResponse create(long postId, CreateCommentRequest request) {
        var comment = new Comment(postId, request.text());
        try {
            var createdComment = commentRepository.create(postId, comment);
            return toResponse(createdComment);
        } catch (DataIntegrityViolationException e) {
            if (!isForeignKeyViolation(e)) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "post " + postId + " not found", e);
        }
    }

    public CommentResponse update(long postId, long id, EditCommentRequest request) {
        var comment = new Comment(request.text());
        var updatedComment = commentRepository.update(postId, id, comment);
        return updatedComment.map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment " + id + " is not found"));
    }

    public void delete(long postId, long id) {
        if (commentRepository.deleteById(postId, id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "comment " + id + " is not found");
        }
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText(), comment.getPostId());
    }

    private static boolean isForeignKeyViolation(DataIntegrityViolationException e) {
        return NestedExceptionUtils.getMostSpecificCause(e) instanceof SQLException sqlException
                && FOREIGN_KEY_VIOLATION_SQL_STATE.equals(sqlException.getSQLState());
    }
}
