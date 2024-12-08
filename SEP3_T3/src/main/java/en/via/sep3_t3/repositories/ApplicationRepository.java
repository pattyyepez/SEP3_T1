package en.via.sep3_t3.repositories;

import en.via.sep3_t3.domain.Application;
import en.via.sep3_t3.repositoryContracts.IApplicationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Qualifier("ApplicationBase")
@Repository
public class ApplicationRepository implements IApplicationRepository {

  private final JdbcTemplate jdbcTemplate;

  public ApplicationRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Application> findAll() {
    String sql = "SELECT * FROM Application";
    return jdbcTemplate.query(sql, new ApplicationRowMapper());
  }

  public Application findById(int listing_id, int sitter_id) {
    String sql = "SELECT * FROM Application WHERE listing_id = ? AND sitter_id = ?";
    return jdbcTemplate.queryForObject(sql, new ApplicationRowMapper(), listing_id, sitter_id);
  }

  public void save(Application application) {
    String sql = "INSERT INTO Application (listing_id, sitter_id, message, status, date) VALUES (?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, application.getListing_id(), application.getSitter_id(),
        application.getMessage(), application.getStatus(),
        new Timestamp(ZonedDateTime.of(application.getDate(), ZoneId.systemDefault()).toInstant().toEpochMilli()));
  }

  public Application update(Application application) {
    String sql = "UPDATE Application SET status = ? WHERE listing_id = ? AND sitter_id = ?";
    jdbcTemplate.update(sql, application.getStatus(),
        application.getListing_id(), application.getSitter_id());
    return findById(application.getListing_id(), application.getSitter_id());
  }

  public void deleteById(int listing_id, int sitter_id) {
    String sql = "DELETE FROM Application WHERE listing_id = ? AND sitter_id = ?";
    jdbcTemplate.update(sql, listing_id, sitter_id);
  }

  private static class ApplicationRowMapper implements RowMapper<Application> {
    @Override
    public Application mapRow(ResultSet rs, int rowNum) throws SQLException {
      Application application = new Application();
      application.setListing_id(rs.getInt("listing_id"));
      application.setSitter_id(rs.getInt("sitter_id"));
      application.setMessage(rs.getString("message"));
      application.setStatus(rs.getString("status"));
      application.setDate(rs.getTimestamp("date").toLocalDateTime());
      return application;
    }
  }
}
