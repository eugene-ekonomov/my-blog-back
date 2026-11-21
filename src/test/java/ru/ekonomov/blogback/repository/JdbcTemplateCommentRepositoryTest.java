package ru.ekonomov.blogback.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.ekonomov.blogback.model.Comment;
import ru.ekonomov.blogback.model.Post;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcTemplateCommentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.update("truncate posts, comments restart identity cascade");
    }

    @Test
    void findAllIfEmpty() {
        var results = commentRepository.findAllByPostId(1L);
        assertEquals(0, results.size());
    }

    @Test
    void findByIdIfEmpty() {
        var results = commentRepository.findById(42L);
        assertEquals(Optional.empty(), results);
    }

    @Test
    void createIfPostDoesntExist() {
        var comment = new Comment(1L, "comment");
        assertThrows(DataIntegrityViolationException.class, () -> commentRepository.create(1L, comment));
    }

    @Test
    void createAndFindAllAndFindById() {
        var post1 = new Post("Title 1", "text 1", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post1).getId();
        var comment1 = new Comment(post1Id, "first comment");
        commentRepository.create(post1Id, comment1);
        var comment2 = new Comment(post1Id, "second comment");
        var comment2Db = commentRepository.create(post1Id, comment2);

        var post2 = new Post("Title 2", "text 2", List.of("tag3", "tag2"));
        var post2Id = postRepository.create(post2).getId();
        var comment21 = new Comment(post2Id, "first comment");
        commentRepository.create(post2Id, comment21);
        var comment22 = new Comment(post2Id, "second comment");
        commentRepository.create(post2Id, comment22);
        var comment23 = new Comment(post2Id, "third comment");
        commentRepository.create(post2Id, comment23);

        var result = commentRepository.findAllByPostId(post1Id);
        assertEquals(2, result.size());
        assertEquals(comment1.getText(), result.get(0).getText());
        assertNotNull(result.get(0).getCreatedAt());
        assertNotNull(result.get(0).getId());
        assertEquals(comment2.getText(), result.get(1).getText());
        assertNotNull(result.get(1).getCreatedAt());
        assertNotNull(result.get(1).getId());

        var comment2Result = commentRepository.findById(comment2Db.getId())
                .orElseThrow(() -> new AssertionError("comment not found"));
        assertEquals(comment2.getText(), comment2Result.getText());
        assertEquals(comment2.getPostId(), comment2Result.getPostId());

        var result2 = commentRepository.findAllByPostId(post2Id);
        assertEquals(3, result2.size());
    }

    @Test
    void commentsCount() {
        var post1 = new Post("Title 1", "text 1", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post1).getId();
        var comment1 = new Comment(post1Id, "first comment");
        var comment1Id = commentRepository.create(post1Id, comment1).getId();
        var comment2 = new Comment(post1Id, "second comment");
        var comment2Id = commentRepository.create(post1Id, comment2).getId();
        var comment3 = new Comment(post1Id, "third comment");
        var comment3Id = commentRepository.create(post1Id, comment3).getId();

        assertEquals(3, postRepository.findById(post1Id)
                .orElseThrow(() -> new AssertionError("post not found"))
                .getCommentsCount());
        commentRepository.deleteById(comment2Id);
        commentRepository.deleteById(comment3Id);
        assertEquals(1, postRepository.findById(post1Id)
                .orElseThrow(() -> new AssertionError("post not found"))
                .getCommentsCount());
        commentRepository.deleteById(comment1Id);
        assertEquals(0, postRepository.findById(post1Id)
                .orElseThrow(() -> new AssertionError("post not found"))
                .getCommentsCount());

    }

    @Test
    void update() {
        var post1 = new Post("Title 1", "text 1", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post1).getId();
        var comment1 = new Comment(post1Id, "first comment");
        commentRepository.create(post1Id, comment1);
        var comment2 = new Comment(post1Id, "second comment");
        var comment2Db = commentRepository.create(post1Id, comment2);

        var comment2new = new Comment(post1Id, "second comment modified");
        assertEquals(comment2new.getText(), commentRepository.update(comment2Db.getId(), comment2new)
                .orElseThrow(() -> new AssertionError("comment not found"))
                .getText());
    }

    @Test
    void updateIfCommentDoesntExist() {
        var comment = new Comment(42L, "some text");
        assertEquals(Optional.empty(), commentRepository.update(42L, comment));
    }

    @Test
    void deleteById() {
        var post1 = new Post("Title 1", "text 1", List.of("tag1", "tag2"));
        var post1Id = postRepository.create(post1).getId();
        var comment1 = new Comment(post1Id, "first comment");
        commentRepository.create(post1Id, comment1);
        var comment2 = new Comment(post1Id, "second comment");
        var comment2Db = commentRepository.create(post1Id, comment2);
        var comment3 = new Comment(post1Id, "third comment");
        commentRepository.create(post1Id, comment3);

        var result = commentRepository.findById(comment2Db.getId())
                .orElseThrow(() -> new AssertionError("comment not found"));

        assertEquals("second comment", result.getText());

        assertEquals(1, commentRepository.deleteById(comment2Db.getId()));

        assertEquals(Optional.empty(), commentRepository.findById(comment2Db.getId()));

        assertEquals(2, commentRepository.findAllByPostId(post1Id).size());
    }

    @Test
    void deleteIfCommentDoesntExist() {
        assertEquals(0, commentRepository.deleteById(42L));
    }
}