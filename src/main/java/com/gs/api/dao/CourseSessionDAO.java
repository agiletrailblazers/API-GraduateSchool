package com.gs.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.gs.api.domain.CourseInstructor;
import com.gs.api.domain.CourseLocation;
import com.gs.api.domain.CourseSession;

@Repository
public class CourseSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(CourseSessionDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.course.session.query}")
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
    public List<CourseSession> getSessions(String id) {
        logger.debug("Getting course competency from database for course id {}", id);
        logger.debug(sql);
        try {
            final List<CourseSession> sessions = this.jdbcTemplate.query(sql, new Object[] { id }, 
                    new SessionsRowMapper());
            logger.debug("Found {} session matchs for {}", sessions.size(), id);
            return sessions;
        } 
        catch (EmptyResultDataAccessException e) {
            logger.warn("Sessions not found for id {} - {}", id, e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving Sessions for id {} - {}", id, e);
            throw e;
        }
    }
    
    /**
     * Maps a course result to a Course object
     */
    protected final class SessionsRowMapper implements RowMapper<CourseSession> {
        /**
         * Map row for Course object from result set
         */
        public CourseSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            CourseSession session = new CourseSession();
            session.setClassNumber(rs.getString("CLASS_NO"));
            session.setSegment(rs.getString("CD_SEG"));
            session.setStartDate(rs.getDate("START_DATE"));
            session.setEndDate(rs.getDate("END_DATE"));
            session.setScheduleMaximum(rs.getInt("QUANTITY_SCHEDULE_MAX"));
            session.setScheduleAvailable(rs.getInt("QUANTITY_SCHEDULE_AVAIL"));
            session.setStatus(rs.getString("STATUS"));
            session.setNotes(rs.getString("NOTES"));
            session.setTuition(rs.getDouble("TUITION"));
            session.setStartTime(rs.getString("START_TIME"));
            session.setEndTime(rs.getString("END_TIME"));
            session.setDays(rs.getString("SESSION_TEMPLATE"));
            CourseLocation location = new CourseLocation();
            location.setId(rs.getString("FACILITY_NO"));
            location.setName(rs.getString("FACILITY_NAME"));
            location.setTelephone(rs.getString("CONTACT_PHONE"));
            location.setAddress1(rs.getString("ADDR1"));
            location.setAddress2(rs.getString("ADDR2"));
            location.setCity(rs.getString("CITY"));
            location.setState(rs.getString("STATE"));
            location.setPostalCode(rs.getString("ZIP"));
            session.setLocation(location);
            if (!StringUtils.isEmpty(rs.getString("PERSON_NO"))) {
                CourseInstructor instructor = new CourseInstructor();
                instructor.setId(rs.getString("PERSON_NO"));
                instructor.setFirstName(rs.getString("FNAME"));
                instructor.setLastName(rs.getString("LNAME"));
                session.setInstructor(instructor);
            }
            return session;
        }
    }
}
