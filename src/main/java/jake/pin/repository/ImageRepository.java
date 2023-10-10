package jake.pin.repository;

import jake.pin.repository.entity.Image;
import jake.pin.repository.entity.ImageListSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.ParameterMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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

    public int remove(Image image) {
        try {
            StringBuilder query = new StringBuilder();
            query.append(" UPDATE /* ImageRepository_remove */ image ");
            query.append(" SET  updated_at = :deletedAt, ");
            query.append("      deleted_at = :deletedAt ");
            query.append(" WHERE id = :id ");
            query.append("   AND user_id = :userId ");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("deletedAt", image.getDeletedAt());
            params.addValue("id", image.getId());
            params.addValue("userId", image.getUserId());

            return jdbcTemplate.update(query.toString(), params);
        } catch (Exception e) {
            log.warn("[ImageRepository:remove] msg: " + e.getMessage(), e);
            return 0;
        }
    }

    public Page<Image> getImages(ImageListSearch request) {
        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append(" SELECT /* ImageRepository_getImages */ ");
        selectQuery.append("    id, ");
        selectQuery.append("    title, ");
        selectQuery.append("    image_url, ");
        selectQuery.append("    view_count ");

        StringBuilder fromQuery = new StringBuilder(" FROM image ");
        StringBuilder whereQuery = new StringBuilder(" WHERE deleted_at IS NULL ");

        Long count = jdbcTemplate.queryForObject("SELECT /* ImageRepository_getImages_count */ count(*) "
                + fromQuery + whereQuery, new ParameterMap<>(), Long.class);

        StringBuilder orderByQuery = new StringBuilder(" ORDER BY created_at desc ");
        orderByQuery.append(" LIMIT :limit OFFSET :offset ");

        StringBuilder query = selectQuery.append(fromQuery).append(whereQuery).append(orderByQuery);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("limit", request.getLimit());
        params.addValue("offset", request.getOffset());

        try {
            PageRequest pageable = PageRequest.of(request.getPage(), request.getLimit(), Sort.Direction.DESC, "createdAt");
            List<Image> images = jdbcTemplate.query(query.toString(), params,
                    rs -> {
                        List<Image> list = new ArrayList<>();
                        int row = 0;
                        while (rs.next()) {
                            Image image = imageMapper.mapRow(rs, row);
                            list.add(image);
                            row++;
                        }
                        return list;
                    }
            );
            return new PageImpl<>(images, pageable, count);
        } catch (Exception e) {
            log.warn("[ImageRepository:getImages] msg: " + e.getMessage(), e);
            return null;
        }
    }

    public Image getImageById(long imageId) {
        try {
            StringBuilder query = new StringBuilder();
            query.append(" SELECT /* ImageRepository_getImageById */ ");
            query.append("      id, ");
            query.append("      image_url, ");
            query.append("      title, ");
            query.append("      description, ");
            query.append("      view_count ");
            query.append(" FROM image ");
            query.append(" WHERE deleted_at IS NULL ");
            query.append("      AND id = :id ");

            Image image = jdbcTemplate.queryForObject(query.toString(), new MapSqlParameterSource("id", imageId), imageMapper);
            return image;
        } catch (Exception e) {
            log.warn("[ImageRepository:getImageById] msg: " + e.getMessage(), e);
            return null;
        }
    }
}
