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
}
