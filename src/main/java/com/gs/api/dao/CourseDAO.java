package com.gs.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

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

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseCredit;
import com.gs.api.domain.CourseCreditType;
import com.gs.api.domain.CourseDescription;
import com.gs.api.domain.CourseLength;

@Repository
public class CourseDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.course.query}")
    private String sql;
    
    @Value("${course.interval.default}")
    private String courseIntervalDefault;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Get course object from database
     * @param id
     * @return Course
     */
    public Course getCourse(String id) {
        logger.debug("Getting course from database for course id {}", id);
        logger.debug(sql);
        try {
            final Course course = this.jdbcTemplate.queryForObject(sql, new Object[] { id, id }, 
                    new CourseRowMapper());
            logger.debug("Found course match for {}", id);
            return course;
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
    protected final class CourseRowMapper implements RowMapper<Course> {
        /**
         * Map row for Course object from result set
         */
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
            Course course = new Course();
            course.setId(rs.getString("CD_CRS"));
            course.setCode(rs.getString("CD_CRS_COURSE"));
            course.setTitle(rs.getString("NM_CRS"));
            course.setDescription(new CourseDescription(rs.getString("DESC_FORMAT")));
            course.setLength(new CourseLength(rs.getString("TM_CD_DUR"), 
                    calculateCourseInterval(rs.getString("TX_CRS_INTERVAL"))));
            course.setType(rs.getString("COURSE_TYPE"));
            course.setCredit(calculateCourseCredit(rs));
            course.setObjective(rs.getString("ABSTRACT"));
            course.setPrerequisites(rs.getString("PREREQUISITES"));
            course.setSegment(rs.getString("CD_SEG"));
    
            return course;
        }

        /**
         * Replace course interval with a default value if empty
         * @param interval
         * @return String
         */
        private String calculateCourseInterval(String interval) {
            if (StringUtils.isEmpty(interval)) {
                interval = courseIntervalDefault;
            }
            return interval;
        }

        /**
         * Calculate course credit based on business rules
         * @param rs
         * @return CourseCredit
         * @throws SQLException
         */
        private CourseCredit calculateCourseCredit(ResultSet rs) throws SQLException {
            String creditValue = StringUtils.EMPTY;
            CourseCreditType creditType = null;
            String ceuCredit = rs.getString("CEU_CREDIT");
            String cpeCredit = rs.getString("CPE_CREDIT");
            String aceCredit = rs.getString("ACE_CREDIT");
            
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

}
