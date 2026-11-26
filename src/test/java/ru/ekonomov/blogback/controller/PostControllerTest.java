package ru.ekonomov.blogback.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.ekonomov.blogback.model.dto.PostResponse;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PostControllerTest extends AbstractWebTest {


    @Test
    void create() throws Exception {
        var json = """
                  {
                      "title": "Название поста 3",
                      "text": "Текст поста в формате Markdown...",
                      "tags": ["tag_1", "tag_2"]
                  }
                """;
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Текст поста в формате Markdown..."))
                .andExpect(jsonPath("$.title").value("Название поста 3"))
                .andExpect(jsonPath("$.tags", contains("tag_1", "tag_2")));
    }

    @Test
    void findById() throws Exception {
        var json = """
                  {
                      "title": "Название поста 3",
                      "text": "Текст поста в формате Markdown...",
                      "tags": ["tag_1", "tag_2"]
                  }
                """;
        var posted = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        mockMvc.perform(get("/api/posts/{id}", getPostResponse(posted).id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Название поста 3"))
                .andReturn();

    }

    @Test
    void findPage() throws Exception {
        var json = """
                  {
                      "title": "Название поста 1",
                      "text": "Текст поста в формате Markdown...",
                      "tags": ["tag_1", "tag_2"]
                  }
                """;
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        var json2 = """
                  {
                      "title": "Название поста 2",
                      "text": "Текст поста в формате Markdown...",
                      "tags": ["tag_21", "tag_22"]
                  }
                """;
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andReturn();

        var json3 = """
                  {
                      "title": "Название поста 3",
                      "text": "Текст поста в формате Markdown...",
                      "tags": ["tag_31", "tag_22"]
                  }
                """;
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json3))
                .andReturn();

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(2)))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.lastPage").value(2))
                .andReturn();
    }

    @Test
    void delete() throws Exception {
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

        var id = getPostResponse(posted).id();
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void update() throws Exception {
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

        var jsonUpdate = """
                  {
                      "title": "Название поста другое",
                      "text": "Другой текст поста в формате Markdown...",
                      "tags": ["tag_11", "tag_12", "tag_13"]
                  }
                """;

        mockMvc.perform(put("/api/posts/{id}", getPostResponse(posted).id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Название поста другое"))
                .andExpect(jsonPath("$.text").value("Другой текст поста в формате Markdown..."))
                .andExpect(jsonPath("$.tags", contains("tag_11", "tag_12", "tag_13")));
    }

    @Test
    void addLike() throws Exception {
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
        var id = getPostResponse(posted).id();

        mockMvc.perform(post("/api/posts/{id}/likes", id))
                .andExpect(content().string("1"))
                .andReturn();
        mockMvc.perform(post("/api/posts/{id}/likes", id))
                .andExpect(content().string("2"))
                .andReturn();
        mockMvc.perform(post("/api/posts/{id}/likes", id))
                .andExpect(content().string("3"))
                .andReturn();
    }

    @Test
    void imageUpdateAndFind() throws Exception {
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

        var id = getPostResponse(posted).id();

        var image = "fake image".getBytes();

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", id)
                        .file(new MockMultipartFile("image", "test.jpg", "image/jpeg", image)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/image", id))
                .andExpect(status().isOk())
                .andExpect(content().bytes(image))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    private static PostResponse getPostResponse(MvcResult posted) throws JsonProcessingException, UnsupportedEncodingException {
        var body = posted.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(body, PostResponse.class);
    }
}
