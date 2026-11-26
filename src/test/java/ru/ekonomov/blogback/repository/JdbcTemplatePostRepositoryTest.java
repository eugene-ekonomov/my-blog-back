package ru.ekonomov.blogback.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.ekonomov.blogback.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class JdbcTemplatePostRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.update("truncate posts, comments restart identity cascade");
    }

    @Test
    void findPageIfEmpty() {
        var results = postRepository.findPage(null, null, 1, 100);
        assertEquals(0, results.size());
    }

    @Test
    void findByIdIfEmpty() {
        var result = postRepository.findById(42);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void createIfFieldsAreNull() {
        var post = new Post(null, null, null);
        assertThrows(DataIntegrityViolationException.class, () -> postRepository.create(post));
    }

    @Test
    void createAndFindAllAndFindById() {
        var results = postRepository.findPage(null, null, 1, 100);
        assertEquals(0, results.size());

        var post = new Post("Message 1", "Message text", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post).getId();
        var post2 = new Post("Message 2", "Message 2 text", List.of("tag1", "tag3"));
        var post2Id = postRepository.create(post2).getId();

        results = postRepository.findPage(null, null, 1, 100);
        assertEquals(2, results.size());
        assertEquals(2, results.get(0).getId());
        assertEquals(1, results.get(1).getId());

        var post1Result = postRepository.findById(post1Id).orElseThrow(() -> new AssertionError("post not found"));
        assertEquals(post.getTitle(), post1Result.getTitle());
        assertEquals(List.of("tag1", "tag2"), post1Result.getTags());
        assertEquals(0, post1Result.getLikesCount());
        assertEquals(0, post1Result.getCommentsCount());

        var post2Result = postRepository.findById(post2Id).orElseThrow(() -> new AssertionError("post not found"));
        assertEquals(post2.getText(), post2Result.getText());
    }

    private void fillForPaginationAndSearch() {
        var post = new Post("first string", "Message text", List.of("tag1", "tag2"));
        postRepository.create(post);
        var post2 = new Post("second", "Message 2 text", List.of("tag1", "tag3"));
        postRepository.create(post2);
        var post3 = new Post("third", "Message 3 text", List.of("tag4", "tag3"));
        postRepository.create(post3);
        var post4 = new Post("fourth", "Message 4 text", List.of("tag5", "tag3"));
        postRepository.create(post4);
        var post5 = new Post("five", "Message 5 text", List.of("tag6", "tag32"));
        postRepository.create(post5);
    }

    @Test
    void pagination() {
        fillForPaginationAndSearch();

        assertEquals(2, postRepository.findPage(null, null, 1, 2).size());
        assertEquals(2, postRepository.findPage(null, null, 2, 2).size());
        assertEquals(1, postRepository.findPage(null, null, 3, 2).size());
        assertEquals(0, postRepository.findPage(null, null, 4, 2).size());
    }

    @Test
    void search() {
        fillForPaginationAndSearch();

        assertEquals(2, postRepository.findPage("", null, 1, 2).size());
        assertEquals(1, postRepository.findPage("first", null, 1, 2).size());
        assertEquals(1, postRepository.findPage("FIRST", null, 1, 2).size());
        assertEquals(1, postRepository.findPage("first string", null, 1, 2).size());
        assertEquals(0, postRepository.findPage("first wrong", null, 1, 2).size());
        assertEquals(1, postRepository.findPage(null, List.of("tag3"), 2, 2).size());
        assertEquals(1, postRepository.findPage(null, List.of("tag3", "tag4"), 1, 2).size());
        assertEquals(1, postRepository.findPage("third", List.of("tag3", "tag4"), 1, 2).size());
        assertEquals(0, postRepository.findPage(null, List.of("doesntexist"), 1, 2).size());

    }

    @Test
    void count() {
        fillForPaginationAndSearch();

        assertEquals(1, postRepository.count("first", null));
        assertEquals(3, postRepository.count(null, List.of("tag3")));
        assertEquals(1, postRepository.count(null, List.of("tag3", "tag4")));
        assertEquals(1, postRepository.count("third", List.of("tag3", "tag4")));
        assertEquals(0, postRepository.count(null, List.of("doesntexist")));
    }

    @Test
    void update() {
        var post1 = new Post("Some title", "Some message", List.of("tag", "othertag"));
        var post1Id = postRepository.create(post1).getId();
        var post1Update = new Post("Some Other title", "Some other message", List.of("othertag", "anothertag"));
        var updatedPost = postRepository.update(post1Id, post1Update).orElseThrow(() -> new AssertionError("post not found"));

        assertEquals("Some Other title", updatedPost.getTitle());
        assertEquals("Some other message", updatedPost.getText());
        assertEquals(List.of("othertag", "anothertag"), updatedPost.getTags());
    }

    @Test
    void updateIfPostDoesntExist() {
        var post1 = new Post("Some title", "Some message", List.of("tag", "othertag"));
        assertEquals(Optional.empty(), postRepository.update(42, post1));
    }

    @Test
    void deleteById() {
        var results = postRepository.findPage(null, null, 1, 100);
        assertEquals(0, results.size());

        var post = new Post("Message 1", "Message text", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post).getId();
        var result = postRepository.findById(post1Id);
        assertEquals("Message 1", result.orElseThrow(() -> new AssertionError("post not found")).getTitle());
        postRepository.deleteById(post1Id);
        var resultAfterDelete = postRepository.findById(post1Id);
        assertEquals(Optional.empty(), resultAfterDelete);
    }

    @Test
    void deleteIfPostDoesntExist() {
        var rowsAffected = postRepository.deleteById(42);
        assertEquals(0, rowsAffected);
    }

    @Test
    void incrementLikesCount() {
        var post = new Post("Message 1", "Message text", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post).getId();

        var results = postRepository.findPage(null, null, 1, 100);
        assertEquals(0, results.getFirst().getLikesCount());

        assertEquals(OptionalInt.of(1), postRepository.incrementLikesCount(post1Id));
        assertEquals(OptionalInt.of(2), postRepository.incrementLikesCount(post1Id));
        assertEquals(OptionalInt.of(3), postRepository.incrementLikesCount(post1Id));

        results = postRepository.findPage(null, null, 1, 100);
        assertEquals(3, results.getFirst().getLikesCount());
    }

    @Test
    void createWithEmptyTags() {
        var post = new Post("Message 1", "Message text", List.of());
        var post1Id = postRepository.create(post).getId();

        var result = postRepository.findById(post1Id);
        assertEquals(List.of(), result.orElseThrow().getTags());
    }

    @Test
    void createWithNullTags() {
        var post = new Post("Message 1", "Message text", null);
        var post1Id = postRepository.create(post).getId();

        var result = postRepository.findById(post1Id);
        assertEquals(List.of(), result.orElseThrow().getTags());
    }

    @Test
    void incrementLikesCountIfPostDoesntExist() {
        assertEquals(OptionalInt.empty(), postRepository.incrementLikesCount(42));
    }

    @Test
    void updateAndFindImage() {
        var post = new Post("Message 1", "Message text", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post).getId();

        var image = "fake image".getBytes();
        assertEquals(1, postRepository.updateImage(post1Id, image));

        var result = postRepository.findImage(post1Id);
        assertArrayEquals(image, result.orElseThrow());
    }

    @Test
    void findImageIfImageDoesntExist() {
        var post = new Post("Message 1", "Message text", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post).getId();

        var result = postRepository.findImage(post1Id);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void findImageIfPostDoesntExist() {
        var result = postRepository.findImage(42);
        assertEquals(Optional.empty(), result);
    }
}