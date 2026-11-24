package ru.ekonomov.blogback.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.ekonomov.blogback.model.Post;
import ru.ekonomov.blogback.model.dto.CreatePostRequest;
import ru.ekonomov.blogback.model.dto.EditPostRequest;
import ru.ekonomov.blogback.model.dto.PostResponse;
import ru.ekonomov.blogback.model.dto.PostsResponse;
import ru.ekonomov.blogback.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private static final int BRIEF_TEXT_LENGTH = 128;

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostsResponse findPage(String search, int pageNumber, int pageSize) {
        if (pageSize < 1 || pageNumber < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        var tags = new ArrayList<String>();
        var words = new ArrayList<String>();
        if (search != null) {
            for (var word : search.split("\\s+")) {
                if (word.isEmpty()) {
                    continue;
                }
                if (word.startsWith("#")) {
                    if (!word.substring(1).isEmpty()) {
                        tags.add(word.substring(1));
                    }
                } else {
                    words.add(word);
                }
            }
        }

        var title = words.isEmpty() ? null : String.join(" ", words);

        var count = postRepository.count(title, tags);

        List<PostResponse> posts = List.of();

        if ((pageNumber - 1) * pageSize < count) {
            posts = postRepository.findPage(title, tags, pageNumber, pageSize).stream()
                    .map(post -> this.toResponse(post, true))
                    .toList();
        }

        var lastPage = (count - 1) / pageSize + 1;

        return new PostsResponse(posts, pageNumber > 1, pageNumber < lastPage, lastPage);
    }

    public PostResponse findById(long id) {
        var post = postRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "post " + id + " not found"));
        return toResponse(post, false);
    }

    public PostResponse create(CreatePostRequest request) {
        var post = new Post(request.title(), request.text(), request.tags());
        var createdPost = postRepository.create(post);
        return toResponse(createdPost, false);
    }

    public PostResponse update(long id, EditPostRequest request) {
        var post = new Post(request.title(), request.text(), request.tags());
        return postRepository.update(id, post)
                .map(updated -> toResponse(updated, false))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post " + id + " not found"));
    }

    public void delete(long id) {
        if (postRepository.deleteById(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "post " + id + " not found");
        }
    }

    public int addLike(long id) {
        return postRepository.incrementLikesCount(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post " + id + " not found"));
    }

    public void updateImage(long id, byte[] image) {
        if (postRepository.updateImage(id, image) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "post " + id + " not found");
        }
    }

    public byte[] findImage(long id) {
        return postRepository.findImage(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "image " + id + " not found"));
    }

    private PostResponse toResponse(Post post, boolean brief) {
        String text;
        if (brief) {
            text = post.getText().length() <= BRIEF_TEXT_LENGTH
                    ? post.getText()
                    : post.getText().substring(0, BRIEF_TEXT_LENGTH) + "...";
        } else {
            text = post.getText();
        }
        return new PostResponse(post.getId(),
                post.getTitle(),
                text,
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }
}
