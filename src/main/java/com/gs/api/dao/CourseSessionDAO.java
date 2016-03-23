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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;

@Repository
public class CourseSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(CourseSessionDAO.class);
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${sql.course.session.query}")
    private String sql;

    @Value("${sql.course.sessions.query}")
    private String sqlForSession;

    @Value("${sql.course.session.single.query}")
    private String sqlForSingleSession;

    @Value("${sql.course.session.all.query}")
    private String sqlForAllSession;

    @Autowired
    public void setDataSource(DataSource dataSource) {

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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
            final List<CourseSession> sessions = this.jdbcTemplate.query(sql, new Object[]{courseId, courseId},
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
     * Get course session details for a specific session.
     * @param sessionId the session id (class number)
     * @return the course session details
     */
    public CourseSession getSession(String sessionId) {
        logger.debug("Getting course session information for sessionId {}", sessionId);
        logger.debug(sqlForSession.concat(sqlForSingleSession));
        try {
            List<String> courseSessionStatus = new ArrayList<String>();
            List<String> courseSessionId = new ArrayList<String>();
            courseSessionStatus.add("C");
            courseSessionStatus.add("S");
            courseSessionId.add(sessionId);
            Map<String,Object>  params = new HashMap<String, Object>();
            params.put("courseSessionStatus",courseSessionStatus);
            params.put("courseSessionId",courseSessionId);

            final CourseSession session = this.namedParameterJdbcTemplate.queryForObject(sqlForSession.concat(sqlForSingleSession), params,
                    new SessionsRowMapper());
            logger.debug("Found session for session id {}", sessionId);
            return session;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            logger.warn("Too many sessions found", e);
            return null;
        }
    }

    public List<CourseSession> getAllSessions(String status,String sessionDomain) {
        logger.debug("Getting course session information for sessionId {}");
        logger.debug(sqlForSession.concat(sqlForAllSession));
        List<String> courseSessionStatus = new ArrayList<String>();
        List<String> courseSessionDomain = new ArrayList<String>();
        try {
            if (status != null ) {
                courseSessionStatus.add(status.toUpperCase());
            } else {
                courseSessionStatus.add("C");
                courseSessionStatus.add("S");
            }
            if (sessionDomain != null ) {
                if (sessionDomain.equalsIgnoreCase("CD")) {
                    courseSessionDomain.add("domin000000000001085");
                } else if (sessionDomain.equalsIgnoreCase("EP")) {
                    courseSessionDomain.add("domin000000000001089");
                } else {
                    courseSessionDomain.add(sessionDomain);
                }
            } else {
                courseSessionDomain.add("domin000000000001085");
                courseSessionDomain.add("domin000000000001089");
            }
            Map<String,Object>  params = new HashMap<String, Object>();
            params.put("courseSessionStatus",courseSessionStatus);
            params.put("courseSessionDomain",courseSessionDomain);
            final List<CourseSession> sessions = this.namedParameterJdbcTemplate.query(sqlForSession.concat(sqlForAllSession),params,
                    new SessionsRowMapper());
            return sessions;
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
            session.setCourseId(rs.getString("COURSE_ID"));
            session.setOfferingSessionId(rs.getString("OFFERING_SESSION_ID"));
            Location location = new Location();
            location.setId(rs.getString("FACILITY_NO"));
            location.setName(rs.getString("FACILITY_NAME"));
            location.setTelephone(rs.getString("CONTACT_PHONE"));
            location.setAddress1(rs.getString("ADDR1"));
            location.setAddress2(rs.getString("ADDR2"));
            location.setCity(rs.getString("CITY"));
            location.setState(rs.getString("STATE"));
            location.setPostalCode(rs.getString("ZIP"));
            session.setCurricumTitle(rs.getString("CURRICUM_TITLE"));
            session.setCourseDomain(rs.getString("COURSE_DOMAIN"));
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
