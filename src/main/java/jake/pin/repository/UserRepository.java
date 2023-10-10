package jake.pin.repository;

import jake.pin.repository.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {

    private final RowMapper<User> userMapper = BeanPropertyRowMapper.newInstance(User.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public User getUserById(long id) {
        try {
            StringBuilder query = new StringBuilder();
            query.append(" SELECT /* UserRepository_getUserById */ id, name");
            query.append(" FROM user ");
            query.append(" WHERE id = :id ");

            return jdbcTemplate.queryForObject(query.toString(), new MapSqlParameterSource("id", id), userMapper);
        } catch (Exception e) {
            log.warn("[UserRepository:getUserById] msg: " + e.getMessage(), e);
            return null;
        }
    }
}
