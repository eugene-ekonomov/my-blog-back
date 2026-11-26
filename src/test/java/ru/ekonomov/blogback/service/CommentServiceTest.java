package ru.ekonomov.blogback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.ekonomov.blogback.model.Comment;
import ru.ekonomov.blogback.model.dto.CreateCommentRequest;
import ru.ekonomov.blogback.model.dto.EditCommentRequest;
import ru.ekonomov.blogback.repository.CommentRepository;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentService commentService;

    @Test
    void createIfPostDoesntExist() {
        var request = new CreateCommentRequest("comment", 42L);
        var fkViolation = new SQLException("violates foreign key constraint \"fk_comments_post\"", "23503");
        when(commentRepository.create(anyLong(), any(Comment.class)))
                .thenThrow(new DataIntegrityViolationException("fk_comments_post", fkViolation));

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.create(42L, request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createRethrowsNonForeignKeyIntegrityViolation() {
        var request = new CreateCommentRequest("comment", 42L);
        var notNullViolation = new SQLException("null value in column \"text\"", "23502");
        when(commentRepository.create(anyLong(), any(Comment.class)))
                .thenThrow(new DataIntegrityViolationException("not null", notNullViolation));

        assertThrows(DataIntegrityViolationException.class, () -> commentService.create(42L, request));
    }

    @Test
    void findByIdIfCommentDoesntExist() {
        when(commentRepository.findById(anyLong(), anyLong())).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.findById(7L, 42L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteIfCommentDoesntExist() {
        when(commentRepository.deleteById(anyLong(), anyLong())).thenReturn(0);

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.delete(7L, 42L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void findAllByPostId() {
        var comments = new ArrayList<Comment>();
        comments.add(new Comment(1L, 1L, "1 comment", ZonedDateTime.now()));
        comments.add(new Comment(2L, 1L, "2 comment", ZonedDateTime.now()));
        comments.add(new Comment(3L, 1L, "3 comment", ZonedDateTime.now()));
        comments.add(new Comment(4L, 1L, "4 comment", ZonedDateTime.now()));
        when(commentRepository.findAllByPostId(1L)).thenReturn(comments);

        var commentResponses = commentService.findAllByPostId(1L);

        assertEquals(4, commentResponses.size());
        assertEquals(comments.get(2).getPostId(), commentResponses.get(2).postId());
        assertEquals(comments.get(2).getText(), commentResponses.get(2).text());
    }

    @Test
    void updateUsesPathIds() {
        var request = new EditCommentRequest(42L, "updated comment", 7L);
        var updated = new Comment(42L, 7L, "updated comment", null);
        when(commentRepository.update(eq(7L), eq(42L), any(Comment.class))).thenReturn(Optional.of(updated));

        var response = commentService.update(7L, 42L, request);

        var captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).update(eq(7L), eq(42L), captor.capture());
        assertNull(captor.getValue().getPostId());
        assertEquals("updated comment", response.text());
    }

    @Test
    void updateIfCommentDoesntExist() {
        var comment = new EditCommentRequest(10L, "text", 42L);
        when(commentRepository.update(anyLong(), anyLong(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.update(10L, 42L, comment));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
