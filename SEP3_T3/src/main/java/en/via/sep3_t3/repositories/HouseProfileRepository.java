package en.via.sep3_t3.repositories;

import en.via.sep3_t3.domain.HouseProfile;
import en.via.sep3_t3.repositoryContracts.IHouseProfileRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Qualifier("HouseProfileBase")
@Repository
public class HouseProfileRepository implements IHouseProfileRepository
{

  private final JdbcTemplate jdbcTemplate;

  public HouseProfileRepository(JdbcTemplate jdbcTemplate)
  {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<HouseProfile> findAll()
  {
    String sql = "SELECT * FROM HouseProfile";
    return jdbcTemplate.query(sql, new HouseProfileRowMapper());
  }

  @Override public List<String> findAllRules()
  {
    String sql = "SELECT * FROM Rules";
    return jdbcTemplate.query(sql, new RulesRowMapper());
  }

  @Override public List<String> findAllChores()
  {
    String sql = "SELECT * FROM Chores";
    return jdbcTemplate.query(sql, new ChoresRowMapper());
  }

  @Override public List<String> findAllAmenities()
  {
    String sql = "SELECT * FROM Amenities";
    return jdbcTemplate.query(sql, new AmenitiesRowMapper());
  }

  public HouseProfile findById(int id)
  {
    String sql = "SELECT * FROM HouseProfile WHERE profile_id = ?";
    return jdbcTemplate.queryForObject(sql, new HouseProfileRowMapper(), id);
  }

  public int save(HouseProfile houseProfile)
  {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
          "INSERT INTO HouseProfile (owner_id, title, description, address, region, city) VALUES (?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, houseProfile.getOwner_id());
      ps.setString(2, houseProfile.getTitle());
      ps.setString(3, houseProfile.getDescription());
      ps.setString(4, houseProfile.getAddress());
      ps.setString(5, houseProfile.getRegion());
      ps.setString(6, houseProfile.getCity());
      return ps;
    }, keyHolder);

    int profileId = (int) keyHolder.getKeys().get("profile_id");

    saveAmenities(houseProfile.getAmenities(), profileId);
    saveChores(houseProfile.getChores(), profileId);
    saveRules(houseProfile.getRules(), profileId);
    savePictures(houseProfile.getPictures(), profileId);

    return profileId;
  }

  public HouseProfile update(HouseProfile houseProfile)
  {
    String sql = "UPDATE HouseProfile SET title = ?, description = ?, address = ?, region = ?, city = ? WHERE profile_id = ?";
    jdbcTemplate.update(sql,
        houseProfile.getTitle(), houseProfile.getDescription(),
        houseProfile.getAddress(), houseProfile.getRegion(),
        houseProfile.getCity(), houseProfile.getId());

    deleteAmenities(houseProfile.getId());
    deleteChores(houseProfile.getId());
    deleteRules(houseProfile.getId());
    deletePictures(houseProfile.getId());

    saveAmenities(houseProfile.getAmenities(), houseProfile.getId());
    saveChores(houseProfile.getChores(), houseProfile.getId());
    saveRules(houseProfile.getRules(), houseProfile.getId());
    savePictures(houseProfile.getPictures(), houseProfile.getId());

    return findById(houseProfile.getId());
  }

  public void deleteById(int id)
  {
    deleteAmenities(id);
    deleteChores(id);
    deleteRules(id);
    deletePictures(id);

    String sql = "DELETE FROM HouseProfile WHERE profile_id = ?";
    jdbcTemplate.update(sql, id);
  }

  private void saveAmenities(List<String> amenities, int profileId)
  {
    if (!(amenities.size() == 1 && amenities.contains("")))
    {
      String sql = "INSERT INTO House_amenities (profile_id, amenity_id) VALUES (?, ?)";
      for (String amenity : amenities)
      {
        jdbcTemplate.update(sql, profileId, getAmenityId(amenity));
      }
    }
  }

  private void deleteAmenities(int profileId)
  {
    String sql = "DELETE FROM House_amenities WHERE profile_id = ?";
    jdbcTemplate.update(sql, profileId);
  }

  private void saveChores(List<String> chores, int profileId)
  {
    if (!(chores.size() == 1 && chores.contains("")))
    {
      String sql = "INSERT INTO House_chores (profile_id, chore_id) VALUES (?, ?)";
      for (String chore : chores)
      {
        jdbcTemplate.update(sql, profileId, getChoreId(chore));
      }
    }
  }

  private void deleteChores(int profileId)
  {
    String sql = "DELETE FROM House_chores WHERE profile_id = ?";
    jdbcTemplate.update(sql, profileId);
  }

  private void saveRules(List<String> rules, int profileId)
  {
    if (!(rules.size() == 1 && rules.contains("")))
    {
      String sql = "INSERT INTO House_rules (profile_id, rule_id) VALUES (?, ?)";
      for (String rule : rules)
      {
        jdbcTemplate.update(sql, profileId, getRuleId(rule));
      }
    }
  }

  private void deleteRules(int profileId)
  {
    String sql = "DELETE FROM House_rules WHERE profile_id = ?";
    jdbcTemplate.update(sql, profileId);
  }

  private void savePictures(List<String> pictures, int profileId)
  {
    String sql = "INSERT INTO House_pictures (profile_id, picture) VALUES (?, ?)";
    for (String picture : pictures)
    {
      jdbcTemplate.update(sql, profileId, picture);
    }
  }

  private void deletePictures(int profileId)
  {
    String sql = "DELETE FROM House_pictures WHERE profile_id = ?";
    jdbcTemplate.update(sql, profileId);
  }

  private int getAmenityId(String amenity)
  {
    String sql = "SELECT id FROM Amenities WHERE type = ?";
    return jdbcTemplate.queryForObject(sql, Integer.class, amenity);
  }

  private int getChoreId(String chore)
  {
    String sql = "SELECT id FROM Chores WHERE type = ?";
    return jdbcTemplate.queryForObject(sql, Integer.class, chore);
  }

  private int getRuleId(String rule)
  {
    String sql = "SELECT id FROM Rules WHERE type = ?";
    return jdbcTemplate.queryForObject(sql, Integer.class, rule);
  }

  private class HouseProfileRowMapper implements RowMapper<HouseProfile>
  {
    @Override public HouseProfile mapRow(ResultSet rs, int rowNum)
        throws SQLException
    {
      HouseProfile houseProfile = new HouseProfile();
      houseProfile.setId(rs.getInt("profile_id"));  // Usa profile_id en lugar de id
      houseProfile.setTitle(rs.getString("title"));
      houseProfile.setDescription(rs.getString("description"));
      houseProfile.setCity(rs.getString("city"));
      houseProfile.setOwner_id(rs.getInt("owner_id"));
      houseProfile.setAddress(rs.getString("address"));
      houseProfile.setRegion(rs.getString("region"));
      houseProfile.setAmenities(fetchAmenities(rs.getInt("profile_id")));
      houseProfile.setChores(fetchChores(rs.getInt("profile_id")));
      houseProfile.setRules(fetchRules(rs.getInt("profile_id")));
      houseProfile.setPictures(fetchPictures(rs.getInt("profile_id")));
      return houseProfile;
    }

    private List<String> fetchAmenities(int profileId)
    {
      String sql = "SELECT type FROM Amenities JOIN House_amenities ON id = amenity_id WHERE profile_id = ?";
      return jdbcTemplate.queryForList(sql, String.class, profileId);
    }

    private List<String> fetchChores(int profileId)
    {
      String sql = "SELECT type FROM Chores JOIN House_chores ON id = chore_id WHERE profile_id = ?";
      return jdbcTemplate.queryForList(sql, String.class, profileId);
    }

    private List<String> fetchRules(int profileId)
    {
      String sql = "SELECT type FROM Rules JOIN House_rules ON id = rule_id WHERE profile_id = ?";
      return jdbcTemplate.queryForList(sql, String.class, profileId);
    }

    private List<String> fetchPictures(int profileId)
    {
      String sql = "SELECT picture FROM House_pictures WHERE profile_id = ?";
      return jdbcTemplate.queryForList(sql, String.class, profileId);
    }
  }

  private class AmenitiesRowMapper implements RowMapper<String>
  {
    @Override public String mapRow(ResultSet rs, int rowNum)
        throws SQLException
    {
      return rs.getString("type");
    }
  }

  private class ChoresRowMapper implements RowMapper<String>
  {
    @Override public String mapRow(ResultSet rs, int rowNum)
        throws SQLException
    {
      return rs.getString("type");
    }
  }

  private class RulesRowMapper implements RowMapper<String>
  {
    @Override public String mapRow(ResultSet rs, int rowNum)
        throws SQLException
    {
      return rs.getString("type");
    }
  }
}
