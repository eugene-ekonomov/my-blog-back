package ru.ekonomov.blogback.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ekonomov.blogback.model.dto.CreatePostRequest;
import ru.ekonomov.blogback.model.dto.EditPostRequest;
import ru.ekonomov.blogback.model.dto.PostResponse;
import ru.ekonomov.blogback.model.dto.PostsResponse;
import ru.ekonomov.blogback.service.PostService;

import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostsResponse findPostsPage(@RequestParam String search,
                                       @RequestParam int pageNumber,
                                       @RequestParam int pageSize) {
        return postService.findPage(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public PostResponse findPostById(@PathVariable long id) {
        return postService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@RequestBody CreatePostRequest request) {
        return postService.create(request);
    }

    @PutMapping("/{id}/image")
    public void updateImage(@PathVariable long id, @RequestPart("image") MultipartFile image) throws IOException {
        postService.updateImage(id, image.getBytes());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> findImage(@PathVariable long id) {
        var result = postService.findImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result);
    }

    @PutMapping("/{id}")
    public PostResponse updatePost(@PathVariable long id,
                                   @RequestBody EditPostRequest request) {
        return postService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable long id) {
        postService.delete(id);
    }

    @PostMapping("/{id}/likes")
    public int addLike(@PathVariable long id) {
        return postService.addLike(id);
    }
}
