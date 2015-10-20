package com.gs.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.googlecode.ehcache.annotations.Cacheable;
import com.gs.api.domain.CourseCategory;
import com.gs.api.domain.CourseSubject;

@Repository
public class CategoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.category.subject.query}")
    private String sql;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Get subject/category object from database
     * @param id
     * @return Subject
     */
    @Cacheable(cacheName="subjectCategoryCache")
    public CourseCategory[] getCategories() {
        logger.debug("Getting course subjects/categories from database");
        logger.debug(sql);
        try {
            final CourseCategory[] categories = this.jdbcTemplate.query(sql,
                    new CategoryExtractor());
            logger.debug("Found {} subjects/categories", categories.length);
            return categories;
        } 
        catch (EmptyResultDataAccessException e) {
            logger.warn("Subjects/categories not found - {}", e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving subjects/categories - {}", e);
            throw e;
        }
    }
    
    /**
     * Map entire result set to a hierarchial array of categories containing subjects
     */
    protected class CategoryExtractor implements ResultSetExtractor<CourseCategory[]> {

        public CourseCategory[] extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<CourseCategory> categories = new ArrayList<CourseCategory>();
            List<CourseSubject> subjects = new ArrayList<CourseSubject>();
            CourseCategory courseCategory = null;
            while (rs.next()) {
                String thisCategory = rs.getString("CATEGORY");
                String thisSubject = rs.getString("SUBJECT");
                String thisSubjectCategory = rs.getString("SUBJECT_CATEGORY");
                logger.debug("Found row of subject: " + thisSubject + " category:" + thisCategory);
                CourseSubject subject = new CourseSubject(thisSubject, thisSubjectCategory, -1);
                if (null == courseCategory) {
                    courseCategory = new CourseCategory();
                }
                else if (!thisCategory.equals(courseCategory.getCategory())) {
                    courseCategory.setCourseSubject(subjects.toArray(new CourseSubject[subjects.size()]));
                    categories.add(courseCategory);
                    courseCategory = new CourseCategory();
                    subjects = new ArrayList<CourseSubject>();
                }
                courseCategory.setCategory(thisCategory);
                subjects.add(subject);
            }
            //always finish up by adding the last item
            if (null != courseCategory) {
                courseCategory.setCourseSubject(subjects.toArray(new CourseSubject[subjects.size()]));
                categories.add(courseCategory);
            }
            return categories.toArray(new CourseCategory[categories.size()]);
        }
    }
}
