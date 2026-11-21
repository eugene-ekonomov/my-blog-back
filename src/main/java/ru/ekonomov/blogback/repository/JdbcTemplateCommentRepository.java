package ru.ekonomov.blogback.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.ekonomov.blogback.model.Comment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcTemplateCommentRepository implements CommentRepository {

    private static final RowMapper<Comment> COMMENT_ROW_MAPPER = (rs, rownum) ->
            new Comment(rs.getLong("id"),
                    rs.getLong("post_id"),
                    rs.getString("text"),
                    rs.getObject("created_at", OffsetDateTime.class).toZonedDateTime()
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comment> findAllByPostId(long postId) {
        return jdbcTemplate.query("""
                          select id, post_id, text, created_at
                            from comments
                           where post_id = ?
                        order by created_at, id
                        """,
                COMMENT_ROW_MAPPER,
                postId);
    }

    @Override
    public Optional<Comment> findById(long id) {
        return jdbcTemplate.query("""
                        select id, post_id, text, created_at
                          from comments
                         where id = ?
                        """,
                COMMENT_ROW_MAPPER,
                id).stream().findFirst();
    }

    @Override
    public Comment create(long postId, Comment comment) {
        return jdbcTemplate.queryForObject("""
                        insert into comments(post_id, text) values(?, ?)
                        returning id, post_id, text, created_at""",
                COMMENT_ROW_MAPPER,
                postId,
                comment.getText());
    }

    @Override
    public Optional<Comment> update(long id, Comment comment) {
        return jdbcTemplate.query("""
                          update comments
                          set text = ?
                        where id = ?
                        returning id, post_id, text, created_at""",
                COMMENT_ROW_MAPPER,
                comment.getText(),
                id).stream().findFirst();
    }

    @Override
    public int deleteById(long id) {
        return jdbcTemplate.update("delete from comments where id = ?", id);
    }
}
