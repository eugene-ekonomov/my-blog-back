package ru.ekonomov.blogback.repository;

import ru.ekonomov.blogback.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface PostRepository {

    List<Post> findPage(String titleSearch, List<String> tags, int pageNumber, int pageSize);

    int count(String titleSearch, List<String> tags);

    Optional<Post> findById(long id);

    Post create(Post post);

    Optional<Post> update(long id, Post post);

    int deleteById(long id);

    OptionalInt incrementLikesCount(long id);

    int updateImage(long id, byte[] image);

    Optional<byte[]> findImage(long id);
}
