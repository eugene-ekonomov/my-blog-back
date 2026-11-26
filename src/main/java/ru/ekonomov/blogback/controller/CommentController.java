package ru.ekonomov.blogback.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.ekonomov.blogback.model.dto.CommentResponse;
import ru.ekonomov.blogback.model.dto.CreateCommentRequest;
import ru.ekonomov.blogback.model.dto.EditCommentRequest;
import ru.ekonomov.blogback.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{postId}/comments")
    public List<CommentResponse> findCommentsByPostId(@PathVariable long postId) {
        return commentService.findAllByPostId(postId);
    }

    @GetMapping("/{postId}/comments/{id}")
    public CommentResponse findCommentById(@PathVariable long postId,
                                           @PathVariable long id) {
        return commentService.findById(postId, id);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@PathVariable long postId,
                                         @Valid @RequestBody CreateCommentRequest request) {
        return commentService.create(postId, request);
    }

    @PutMapping("/{postId}/comments/{id}")
    public CommentResponse updateComment(@PathVariable long postId,
                                         @PathVariable long id,
                                         @Valid @RequestBody EditCommentRequest request) {
        return commentService.update(postId, id, request);
    }

    @DeleteMapping("/{postId}/comments/{id}")
    public void deleteComment(@PathVariable long postId,
                              @PathVariable long id) {
        commentService.delete(postId, id);
    }
}
