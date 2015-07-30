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

@Repository
public class CourseCompetencyDAO {

    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.course.competency.query}")
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
    public List<String> getCompetency(String id) {
        logger.debug("Getting course competency from database for course id {}", id);
        logger.debug(sql);
        try {
            final List<String> competency = this.jdbcTemplate.query(sql, new Object[] { id }, 
                    new CompetencyRowMapper());
            logger.debug("Found {} competency matchs for {}", competency.size(), id);
            return competency;
        } 
        catch (EmptyResultDataAccessException e) {
            logger.warn("Course not found for id {} - {}", id, e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving Course for id {} - {}", id, e);
            throw e;
        }
    }
    
    /**
     * Maps a course result to a Course object
     */
    protected final class CompetencyRowMapper implements RowMapper<String> {
        /**
         * Map row for Course object from result set
         */
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("NAME");
        }
    }
}
