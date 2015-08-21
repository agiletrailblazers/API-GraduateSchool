package com.gs.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.googlecode.ehcache.annotations.Cacheable;
import com.gs.api.domain.Location;

@Repository
public class LocationDAO {

    private static final Logger logger = LoggerFactory.getLogger(LocationDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.location.city.state.query}")
    private String sql;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Get course object from database
     * @param id
     * @return Course
     */
    @Cacheable(cacheName="locationByCityStateCache")
    public List<Location> getLocationByCityState() {
        logger.debug("Getting course locations from database");
        logger.debug(sql);
        try {
            final List<Location> locations = this.jdbcTemplate.query(sql,
                    new LocationRowMapper());
            logger.debug("Found {} locations", locations.size());
            return locations;
        } 
        catch (EmptyResultDataAccessException e) {
            logger.warn("Locations not found - {}", e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving locations - {}", e);
            throw e;
        }
    }
    
    /**
     * Maps a course result to a Course object
     */
    protected final class LocationRowMapper implements RowMapper<Location> {
        /**
         * Map row for Course object from result set
         */
        public Location mapRow(ResultSet rs, int rowNum) throws SQLException {
            Location location = new Location();
            location.setCity(rs.getString("CITY"));
            location.setState(rs.getString("STATE"));
            return location;
        }
    }
}
