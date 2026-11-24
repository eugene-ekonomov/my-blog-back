package ru.ekonomov.blogback.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.ekonomov.blogback.model.Comment;
import ru.ekonomov.blogback.model.dto.CommentResponse;
import ru.ekonomov.blogback.model.dto.CreateCommentRequest;
import ru.ekonomov.blogback.model.dto.EditCommentRequest;
import ru.ekonomov.blogback.repository.CommentRepository;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentResponse> findAllByPostId(long postId) {
        return commentRepository.findAllByPostId(postId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CommentResponse findById(long id) {
        return commentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment " + id + " is not found"));
    }

    public CommentResponse create(long postId, CreateCommentRequest request) {
        var comment = new Comment(postId, request.text());
        try {
            var createdComment = commentRepository.create(postId, comment);
            return toResponse(createdComment);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "post " + postId + " not found", e);
        }
    }

    public CommentResponse update(long id, EditCommentRequest request) {
        var comment = new Comment(request.text());
        var updatedComment = commentRepository.update(id, comment);
        return updatedComment.map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment " + id + " is not found"));
    }

    public void delete(long id) {
        if (commentRepository.deleteById(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "comment " + id + " is not found");
        }
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText(), comment.getPostId());
    }
}
