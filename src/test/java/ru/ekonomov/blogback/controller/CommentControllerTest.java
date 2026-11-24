package ru.ekonomov.blogback.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.ekonomov.blogback.model.dto.CommentResponse;
import ru.ekonomov.blogback.model.dto.PostResponse;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CommentControllerTest extends AbstractWebTest {

    @Test
    void create() throws Exception {
        var postId = createPostAndGetPostId();

        var jsonComment = """
                  {
                    "text": "Комментарий к посту",
                    "postId": %s
                  }
                """.formatted(postId);

        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComment))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Комментарий к посту"));
    }

    @Test
    void update() throws Exception {
        var postId = createPostAndGetPostId();

        var id = createCommentAndGetId(postId, "Комментарий");

        var updatedComment = """
                  {
                    "id": %s,
                    "text": "Исправленный комментарий к посту",
                    "postId": %s
                  }
                """.formatted(id, postId);

        mockMvc.perform(put("/api/posts/{postId}/comments/{id}", postId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedComment))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Исправленный комментарий к посту"));
    }

    @Test
    void delete() throws Exception {
        var postId = createPostAndGetPostId();

        createCommentAndGetId(postId, "Комментарий 1");
        createCommentAndGetId(postId, "Комментарий 2");
        var id3 = createCommentAndGetId(postId, "Комментарий 3");
        createCommentAndGetId(postId, "Комментарий 4");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/{postId}/comments/{id}", postId, id3))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{postId}/comments/{id}", postId, id3))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void findByPostId() throws Exception {
        var postId = createPostAndGetPostId();

        createCommentAndGetId(postId, "Комментарий 1");
        createCommentAndGetId(postId, "Комментарий 2");
        createCommentAndGetId(postId, "Комментарий 3");
        createCommentAndGetId(postId, "Комментарий 4");

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].text", contains(
                        "Комментарий 1", "Комментарий 2", "Комментарий 3", "Комментарий 4")))
                .andExpect(jsonPath("$[0].postId").value(postId));
    }

    @Test
    void findById() throws Exception {
        var postId = createPostAndGetPostId();

        createCommentAndGetId(postId, "Комментарий 1");
        var id2 = createCommentAndGetId(postId, "Комментарий 2");

        mockMvc.perform(get("/api/posts/{postId}/comments/{id}", postId, id2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Комментарий 2"));
    }

    private static PostResponse getPostResponse(MvcResult posted) throws JsonProcessingException, UnsupportedEncodingException {
        var body = posted.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(body, PostResponse.class);
    }

    private static CommentResponse getCommentResponse(MvcResult commented) throws JsonProcessingException, UnsupportedEncodingException {
        var body = commented.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(body, CommentResponse.class);
    }

    private long createPostAndGetPostId() throws Exception {
        var json = """
                  {
                      "title": "Название поста 1",
                      "text": "Текст поста в формате Markdown...",
                      "tags": ["tag_1", "tag_2"]    
                  }
                """;
        var posted = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        return getPostResponse(posted).id();
    }

    private long createCommentAndGetId(long postId, String text) throws Exception {
        var jsonComment = """
                  {
                    "text": "%s",
                    "postId": %s
                  }
                """.formatted(text, postId);

        var comment = mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComment))
                .andReturn();

        return getCommentResponse(comment).id();
    }
}
