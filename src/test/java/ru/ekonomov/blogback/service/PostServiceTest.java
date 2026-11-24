package ru.ekonomov.blogback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.ekonomov.blogback.model.Post;
import ru.ekonomov.blogback.model.dto.EditPostRequest;
import ru.ekonomov.blogback.repository.PostRepository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostService postService;

    @Test
    void findPageParsesTagsAndTitle() {
        when(postRepository.count("first string", List.of("tag1"))).thenReturn(0);
        postService.findPage("#tag1 first string", 1, 5);

        verify(postRepository).count("first string", List.of("tag1"));
        verify(postRepository, never()).findPage(any(), any(), anyInt(), anyInt());
    }

    @Test
    void findPageComputesFlagsOnLastPage() {
        when(postRepository.count(null, List.of())).thenReturn(4);
        when(postRepository.findPage(null, List.of(), 2, 2)).thenReturn(List.of());

        var response = postService.findPage("", 2, 2);

        assertEquals(2, response.lastPage());
        assertTrue(response.hasPrev());
        assertFalse(response.hasNext());
    }

    @Test
    void findPageTruncatesLongTextAndFindById() {
        var longText = "a".repeat(200);
        var post = new Post(1L, "title", longText, List.of(), 0, 0, null);

        when(postRepository.count(null, List.of())).thenReturn(1);
        when(postRepository.findPage(null, List.of(), 1, 5)).thenReturn(List.of(post));

        var response = postService.findPage("", 1, 5);

        assertEquals(131, response.posts().getFirst().text().length());
        assertTrue(response.posts().getFirst().text().endsWith("..."));

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        var responseById = postService.findById(1);
        assertEquals(longText, responseById.text());
    }

    @Test
    void findPageComputesFlags() {
        when(postRepository.count(null, List.of())).thenReturn(5);
        when(postRepository.findPage(null, List.of(), 2, 2)).thenReturn(List.of());

        var response = postService.findPage("", 2, 2);

        assertEquals(3, response.lastPage());
        assertTrue(response.hasPrev());
        assertTrue(response.hasNext());
    }

    @Test
    void findPageRejectsInvalidParams() {
        var ex = assertThrows(ResponseStatusException.class, () -> postService.findPage("", 0, 5));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void findPageIgnoresEmptyTokensAndBareHash() {
        when(postRepository.count(null, List.of())).thenReturn(0);

        postService.findPage(" # ", 1, 5);

        verify(postRepository).count(null, List.of());
    }

    @Test
    void updateIfPostDoesntExist() {
        when(postRepository.update(anyLong(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () ->
                postService.update(42L, new EditPostRequest(42L, "title", "text", List.of())));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteIfPostDoesntExist() {
        when(postRepository.deleteById(anyLong())).thenReturn(0);

        var ex = assertThrows(ResponseStatusException.class, () -> postService.delete(42L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void addLikeIfPostDoesntExist() {
        when(postRepository.incrementLikesCount(anyLong())).thenReturn(OptionalInt.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> postService.addLike(42L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateImageIfPostDoesntExist() {
        when(postRepository.updateImage(anyLong(), any())).thenReturn(0);

        var ex = assertThrows(ResponseStatusException.class, () -> postService.updateImage(42L, new byte[]{}));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void findImageIfPostDoesntExist() {
        when(postRepository.findImage(anyLong())).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> postService.findImage(42L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
