package jake.pin.repository;

import jake.pin.repository.entity.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ImageRepository {

    private final RowMapper<Image> imageMapper = BeanPropertyRowMapper.newInstance(Image.class);
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long save(Image image) {
        try {
            StringBuilder query = new StringBuilder();
            query.append(" INSERT INTO /* ImageRepository_save */ image ");
            query.append(" (user_id, title, description, image_url, created_at, updated_at) ");
            query.append(" VALUES ");
            query.append(" (:userId, :title, :description, :imageUrl, :createdAt, :createdAt) ");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("userId", image.getUserId());
            params.addValue("title", image.getTitle());
            params.addValue("description", image.getDescription());
            params.addValue("imageUrl", image.getImageUrl());
            params.addValue("createdAt", image.getCreatedAt());

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(query.toString(), params, keyHolder);

            return (Long) keyHolder.getKeys().get("ID");
        } catch (Exception e) {
            log.warn("[ImageRepository:save] msg: " + e.getMessage(), e);
            return 0L;
        }
    }

    public Image getImageByIdAndUserId(long id, long userId) {
        try {
            StringBuilder query = new StringBuilder();
            query.append(" SELECT /* ImageRepository_getImageByIdAndUserId */ ");
            query.append("      id, ");
            query.append("      image_url, ");
            query.append("      title, ");
            query.append("      description, ");
            query.append("      view_count ");
            query.append(" FROM image ");
            query.append(" WHERE deleted_at IS NULL ");
            query.append("      AND id = :id ");
            query.append("      AND user_id = :userId ");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            params.addValue("userId", userId);

            return jdbcTemplate.queryForObject(query.toString(), params, imageMapper);
        } catch (Exception e) {
            log.warn("[ImageRepository:getImageByIdAndUserId] msg: " + e.getMessage(), e);
            return null;
        }
    }

    public int modify(Image image) {
        try {
            StringBuilder query = new StringBuilder();
            query.append(" UPDATE /* ImageRepository_modify */ image ");
            query.append(" SET  title = :title, ");
            query.append("      description = :description, ");
            query.append("      updated_at = :updatedAt ");
            query.append(" WHERE id = :id ");
            query.append("   AND user_id = :userId ");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("title", image.getTitle());
            params.addValue("description", image.getDescription());
            params.addValue("updatedAt", image.getUpdatedAt());
            params.addValue("id", image.getId());
            params.addValue("userId", image.getUserId());

            return jdbcTemplate.update(query.toString(), params);
        } catch (Exception e) {
            log.warn("[ImageRepository:modify] msg: " + e.getMessage(), e);
            return 0;
        }
    }
}
