package ru.ekonomov.blogback.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.ekonomov.blogback.model.Post;

import java.sql.Array;
import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class JdbcTemplatePostRepository implements PostRepository {

    private static final RowMapper<Post> POST_ROW_MAPPER = (rs, rownum) -> {
        Array sqlArray = rs.getArray("tags");
        List<String> tags = Arrays.asList((String[]) sqlArray.getArray());
        return new Post(rs.getLong("id"),
                rs.getString("title"),
                rs.getString("text"),
                tags,
                rs.getInt("likes_count"),
                rs.getInt("comments_count"),
                rs.getObject("created_at", OffsetDateTime.class).toZonedDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplatePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findPage(String titleSearch, List<String> tags, int pageNumber, int pageSize) {
        String[] tagFilter = tags == null ? new String[0] : tags.toArray(String[]::new);
        String titlePattern = titleSearch == null || titleSearch.isBlank()
                ? null
                : "%" + titleSearch + "%";

        return jdbcTemplate.query("""
                        select ps.id, ps.title, ps.text, ps.tags, ps.likes_count,
                                (select count(*) from comments c where c.post_id = ps.id) as comments_count,
                                ps.created_at
                          from posts ps
                         where ps.tags @> cast(? as text[])
                           and (cast(? as text) is null or ps.title ilike cast(? as text))
                         order by ps.created_at desc, ps.id desc
                         limit ? offset ?""",
                POST_ROW_MAPPER,
                tagFilter,
                titlePattern,
                titlePattern,
                pageSize,
                (pageNumber - 1) * pageSize);
    }

    @Override
    public int count(String titleSearch, List<String> tags) {
        String[] tagFilter = tags == null ? new String[0] : tags.toArray(String[]::new);
        String titlePattern = titleSearch == null || titleSearch.isBlank()
                ? null
                : "%" + titleSearch + "%";

        Integer total = jdbcTemplate.queryForObject("""
                        select count(*)
                          from posts
                         where tags @> cast(? as text[])
                           and (cast(? as text) is null or title ilike cast(? as text))""",
                Integer.class,
                tagFilter,
                titlePattern,
                titlePattern);

        return Objects.requireNonNullElse(total, 0);
    }

    @Override
    public Optional<Post> findById(long id) {
        return jdbcTemplate.query("""
                         select ps.id, ps.title, ps.text, ps.tags, ps.likes_count, (select count(*) from comments c where c.post_id = ps.id) comments_count, ps.created_at
                         from posts ps
                        where ps.id = ?""",
                POST_ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    @Override
    public Post create(Post post) {
        return jdbcTemplate.queryForObject("""
                        insert into posts(title, text, tags) values(?, ?, ?)
                        returning id, title, text, tags, likes_count, 0 comments_count, created_at""",
                POST_ROW_MAPPER,
                post.getTitle(),
                post.getText(),
                Optional.ofNullable(post.getTags())
                        .orElse(List.of())
                        .toArray(String[]::new));
    }

    @Override
    public Optional<Post> update(long id, Post post) {
        return jdbcTemplate.query("""
                              update posts ps
                              set title = ?,
                                  text = ?,
                                  tags = ?
                            where ps.id = ?
                        returning ps.id, ps.title, ps.text, ps.tags, ps.likes_count, 
                        (select count(*) from comments c where c.post_id = ps.id) as comments_count, 
                        ps.created_at""",
                POST_ROW_MAPPER,
                post.getTitle(),
                post.getText(),
                Optional.ofNullable(post.getTags())
                        .orElse(List.of())
                        .toArray(String[]::new),
                id).stream().findFirst();
    }

    @Override
    public int deleteById(long id) {
        return jdbcTemplate.update("delete from posts where id = ?", id);
    }

    @Override
    public OptionalInt incrementLikesCount(long id) {
        return jdbcTemplate.query("""
                           update posts
                              set likes_count = likes_count + 1
                            where id = ?
                        returning likes_count""",
                rs -> rs.next() ? OptionalInt.of(rs.getInt(1)) : OptionalInt.empty(),
                id);
    }

    @Override
    public int updateImage(long id, byte[] image) {
        return jdbcTemplate.update("""
                        update posts
                           set image_data = ?
                         where id = ?""",
                image,
                id);
    }

    @Override
    public Optional<byte[]> findImage(long id) {
        var results = jdbcTemplate.query("""
                        select image_data
                          from posts
                         where id = ?""",
                (res, rownum) -> res.getBytes("image_data"),
                id);
        return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.getFirst());
    }
}
