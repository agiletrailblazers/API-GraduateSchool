package com.gs.api.dao;

import com.gs.api.domain.course.CourseInstructor;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.course.Location;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

@Repository
public class CourseSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(CourseSessionDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.course.session.query}")
    private String sql;

    @Value("${sql.course.session.single.query}")
    private String sqlForSingleSession;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get course object from database
     * @param courseId Load this course by this ID
     * @return Course
     */
    public List<CourseSession> getSessions(String courseId) {
        logger.debug("Getting course competency from database for course id {}", courseId);
        logger.debug(sql);
        try {
            final List<CourseSession> sessions = this.jdbcTemplate.query(sql, new Object[] { courseId, courseId },
                    new SessionsRowMapper());
            logger.debug("Found {} session matchs for {}", sessions.size(), courseId);
            return sessions;
        }
        catch (EmptyResultDataAccessException e) {
            logger.warn("Sessions not found for course id {} - {}", courseId, e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving Sessions for course id {} - {}", courseId, e);
            throw e;
        }
    }

    /**
     * Get course session details for a specific course/session.
     * @param courseId the course id
     * @param sessionId the session id (class number)
     * @return the course session details
     */
    public CourseSession getSession(String courseId, String sessionId) {
        logger.debug("Getting course session information for course id {}, sessionId {}", courseId, sessionId);
        logger.debug(sql);
        try {
            final CourseSession session = this.jdbcTemplate.queryForObject(sqlForSingleSession, new Object[] { courseId, courseId, sessionId },
                    new SessionsRowMapper());
            logger.debug("Found session for course id {}, session id {}", courseId, sessionId);
            return session;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            logger.warn("Too many sessions found", e);
            return null;
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
            session.setScheduleMaximum(rs.getInt("MAX_STUD_COUNT"));
            session.setScheduleAvailable(rs.getInt("REGISTERED_STUD_COUNT"));
            session.setScheduleMinimum(rs.getInt("MIN_STUD_COUNT"));
            session.setStatus(rs.getString("STATUS"));
            session.setNotes(rs.getString("NOTES"));
            session.setTuition(rs.getDouble("TUITION"));
            session.setStartTime(rs.getString("START_TIME"));
            session.setEndTime(rs.getString("END_TIME"));
            session.setDays(rs.getString("SESSION_TEMPLATE"));
            Location location = new Location();
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
