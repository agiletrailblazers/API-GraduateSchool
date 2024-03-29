package com.gs.api.dao;

import com.gs.api.domain.course.CourseInstructor;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.course.Location;

import com.gs.api.search.util.SessionQueryParamsBuilder;
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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

@Repository
public class CourseSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(CourseSessionDAO.class);
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${sql.course.sessions.query}")
    private String sql;

    @Value("${sql.course.session.query}")
    private String sqlForSessions;

    @Value("${sql.course.session.whereClause.sessionId}")
    private String sqlForSessionById;

    @Value("${sql.course.session.whereClause.sessionDomain}")
    private String sqlForSessionsByDomain;


    @Autowired
    private SessionQueryParamsBuilder sessionQueryParamsBuilder;

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
    public List<CourseSession> getSessionsByCourseId(String courseId) {
        logger.debug("Getting course competency from database for course id {} - {}", courseId , sql);
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
    public CourseSession getSessionById(String sessionId)  {
        logger.debug("Getting course session information for sessionId {}", sessionId);
        String sessionIdQuery = sqlForSessions.concat(sqlForSessionById);
        logger.debug(sessionIdQuery);
        Map<String,Object> params = sessionQueryParamsBuilder.buildSessionQueryParams(sessionId);
        try {
            final CourseSession session = this.namedParameterJdbcTemplate.queryForObject(sessionIdQuery, params,
                    new SessionsRowMapper());
            logger.debug("Found session for session id {}", sessionId);
            return session;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            logger.warn("Too many sessions found", e);
            return null;
        }
    }

    /**
     * Get  all active course sessions details
     * @param status the session status C-G2G
     * @param sessionDomain the session domain Type
     * @return the list of course session details
     */
    public List<CourseSession> getSessions(String status,String sessionDomain) {
        logger.debug("Getting course sessions information for status {} - {}",status,sessionDomain);
        String courseSessionsQuery = sqlForSessions;
        if (StringUtils.isNotEmpty(sessionDomain)) {
            courseSessionsQuery = courseSessionsQuery.concat(sqlForSessionsByDomain);
        }
        logger.debug(courseSessionsQuery);
        try {
            Map<String,Object> params = sessionQueryParamsBuilder.buildCourseSessionsQueryParams(status, sessionDomain);
            final List<CourseSession> sessions = this.namedParameterJdbcTemplate.query(courseSessionsQuery,params,
                    new SessionsRowMapper());
            logger.debug("Sessions Found");
            return sessions;
        }
        catch (EmptyResultDataAccessException e) {
            logger.debug("Sessions not found", e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving Sessions", e);
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
            Location facility = new Location();
            facility.setId(rs.getString("FACILITY_NO"));
            facility.setName(rs.getString("FACILITY_NAME"));
            facility.setTelephone(rs.getString("FAC_CONTACT_PHONE"));
            facility.setAddress1(rs.getString("FAC_ADDR1"));
            facility.setAddress2(rs.getString("FAC_ADDR2"));
            facility.setCity(rs.getString("FAC_CITY"));
            facility.setState(rs.getString("FAC_STATE"));
            facility.setPostalCode(rs.getString("FAC_ZIP"));
            Location location = new Location();
            location.setId(rs.getString("LOCATION_NO"));
            location.setName(rs.getString("LOCATION_NAME"));
            location.setTelephone(rs.getString("LOC_CONTACT_PHONE"));
            location.setAddress1(rs.getString("LOC_ADDR1"));
            location.setAddress2(rs.getString("LOC_ADDR2"));
            location.setCity(rs.getString("LOC_CITY"));
            location.setState(rs.getString("LOC_STATE"));
            location.setPostalCode(rs.getString("LOC_ZIP"));
            session.setCurricumTitle(rs.getString("CURRICUM_TITLE"));
            session.setCurricumTabDisplayOrder(rs.getInt("CURRICUMTABDISPLAYORDER"));
            session.setFacility(facility);
            session.setLocation(location);
            if (!StringUtils.isEmpty(rs.getString("PERSON_NO"))) {
                CourseInstructor instructor = new CourseInstructor();
                instructor.setId(rs.getString("PERSON_NO"));
                instructor.setFirstName(rs.getString("FNAME"));
                instructor.setLastName(rs.getString("LNAME"));
                session.setInstructor(instructor);
            }
            session.setCourseCode(rs.getString("CD_CRS_COURSE"));
            session.setCourseTitle(rs.getString("NM_CRS"));
            return session;
        }
    }
}
