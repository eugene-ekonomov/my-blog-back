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
        when(commentRepository.create(anyLong(), any(Comment.class)))
                .thenThrow(new DataIntegrityViolationException("fk_comments_post"));

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.create(42L, request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void findByIdIfCommentDoesntExist() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.findById(42L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteIfCommentDoesntExist() {
        when(commentRepository.deleteById(anyLong())).thenReturn(0);

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.delete(42L));
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
    void updateIgnoresPostId() {
        var request = new EditCommentRequest(7L, "updated comment", 42L);
        var updated = new Comment(7L, 42L, "updated comment", null);
        when(commentRepository.update(eq(7L), any(Comment.class))).thenReturn(Optional.of(updated));

        var response = commentService.update(7L, request);

        var captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).update(eq(7L), captor.capture());
        assertNull(captor.getValue().getPostId());
        assertEquals("updated comment", response.text());
    }

    @Test
    void updateIfCommentDoesntExist() {
        var comment = new EditCommentRequest(42L, "text", 10L);
        when(commentRepository.update(anyLong(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> commentService.update(42L, comment));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
