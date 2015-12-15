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

import com.googlecode.ehcache.annotations.Cacheable;
import com.gs.api.domain.Course;
import com.gs.api.domain.CourseCredit;
import com.gs.api.domain.CourseCreditType;
import com.gs.api.domain.CourseDescription;
import com.gs.api.domain.CourseIntervalConvertor;
import com.gs.api.domain.CourseLength;

@Repository
public class CourseDAO {

    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.course.single.query}")
    private String singleCourseSql;

    @Value("${sql.course.all.query}")
    private String allCourseSql;

    @Value("${course.interval.default}")
    private String courseIntervalDefault;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get course object from database
     * @param id load this course by ID
     * @return Course
     */
    public Course getCourse(String idOrCode) {
        logger.debug("Getting course from database for course id {}", idOrCode);
        logger.debug(singleCourseSql);
        try {
            final Course course = this.jdbcTemplate.queryForObject(singleCourseSql, new Object[] { idOrCode, idOrCode },
                    new CourseDetailRowMapper());
            logger.debug("Found course match for {}", idOrCode);
            return course;
        }
        catch (EmptyResultDataAccessException e) {
            logger.warn("Course not found for id {} - {}", idOrCode, e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving Course for id {} - {}", idOrCode, e);
            throw e;
        }
    }

    /**
     * Get all active courses
     * @return List of courses
     */
    @Cacheable(cacheName="allCourses")
    public List<Course> getCourses() throws Exception {
        logger.debug("Getting courses from database");
        logger.debug(allCourseSql);
        try {
            List<Course> courses = this.jdbcTemplate.query(allCourseSql,
                    new CourseRowMapper());
            logger.debug("Found {} courses", courses.size());
            return courses;
        }
        catch (EmptyResultDataAccessException e) {
            logger.warn("Courses not found - {}", e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving courses - {}", e);
            throw e;
        }
    }

    /**
     * Maps a course result to a Course object
     */
    protected class CourseRowMapper implements RowMapper<Course> {
        /**
         * Map row for Course object from result set - basic fields
         */
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
            Course course = new Course();
            course.setId(rs.getString("CD_CRS"));
            course.setCode(rs.getString("CD_CRS_COURSE"));
            course.setTitle(rs.getString("NM_CRS"));
            course.setType(rs.getString("TYPE"));
            return course;
        }

        /**
         * Replace course interval with a default value if empty
         * @param interval a string representing time that needs converting to 'how long' course is
         * @return String
         */
        protected String calculateCourseInterval(String interval) {
            interval = CourseIntervalConvertor.getInterval(interval);
            if (StringUtils.isEmpty(interval)) {
                interval = courseIntervalDefault;
            }
            return interval;
        }

        /**
         * Calculate course credit based on business rules
         * @param resultSet How many credits does a class count for (From SQL)
         * @return CourseCredit
         * @throws SQLException
         */
        protected CourseCredit calculateCourseCredit(ResultSet resultSet) throws SQLException {
            String creditValue;
            CourseCreditType creditType;
            String ceuCredit = resultSet.getString("CEU_CREDIT");
            String cpeCredit = resultSet.getString("CPE_CREDIT");
            String aceCredit = resultSet.getString("ACE_CREDIT");

            if (StringUtils.isNotEmpty(ceuCredit) && !ceuCredit.equals("0")) {
                creditValue = ceuCredit;
                creditType = CourseCreditType.CEU;
            }
            else if (StringUtils.isNotEmpty(cpeCredit) && !cpeCredit.equals("0")) {
                creditValue = cpeCredit;
                creditType = CourseCreditType.CPE;
            }
            else if (StringUtils.isNotEmpty(aceCredit) && !aceCredit.equals("0")) {
                creditValue = aceCredit;
                creditType = CourseCreditType.ACE;
            }
            else {
                logger.info("No match found for Course Credit");
                return null;
            }
            return new CourseCredit(creditValue, creditType);
        }
    }

    protected class CourseDetailRowMapper extends CourseRowMapper implements RowMapper<Course> {

        @Override
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
             Course course = super.mapRow(rs, rowNum);
             course.setDescription(new CourseDescription(cleanupNewlines(rs.getString("DESC_FORMAT"))));
             String interval = calculateCourseInterval(rs.getString("TX_CRS_INTERVAL"));
             course.setLength(new CourseLength(rs.getInt("TM_CD_DUR"), interval));
             course.setCredit(calculateCourseCredit(rs));
             course.setObjective(cleanupNewlines(rs.getString("ABSTRACT")));
             course.setPrerequisites(cleanupNewlines(rs.getString("PREREQUISITES")));
             course.setSegment(rs.getString("CD_SEG"));
             return course;
        }

        /**
         * Remove newline chars
         * @param in
         * @return clean string
         */
        private String cleanupNewlines(String in) {
            return StringUtils.remove(StringUtils.remove(in, "\n"), "\r");
        }
        
    }
   

}
