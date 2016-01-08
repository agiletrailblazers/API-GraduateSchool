package com.gs.registration.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.googlecode.ehcache.annotations.Cacheable;

@Repository
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall simpleJdbcCall;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("read_company");
    }

    /**
     * Insert a new user into the database
     * @param id load this course by ID
     * @return Course
     */
    public boolean insertNewUser(char id, String ts, String title, String personNo, String fjrstName, String lastName,
                                 String middleName, String homePhone, String workPhone, String fax, String createdBy,
                                 Date createdOn, String updatedBy, Date updatedOn, String custom0, String custom1,
                                 String custom2, char companyId, String address1,String address2, String address3,
                                 String city, String state, String zip, String country, String email, String split,
                                 String custom3, String custom4, char desJbtypId, char localeId, String password,
                                 String userName, char ManagerId, char timeZoneId, char corres_pref1, char corres_pref2,
                                 char corresPref3, String custom5, String custom6, String custom7, String custom8,
                                 String custom9, char currencyId, String suffix, String jobTitle, String ssNo, String Status,
                                 String personType, char locationId, char homeDomain, String accountNo, char gender,
                                 Date startedOn, Date terminatedOn, Date dateOfBirth, String ethnicity, String religion,
                                 String secretQuestion, String secretAnswer, char passwordChanged, String newTs) {
        //corres_pref1 + corres_pref2 + corres_pref_3 + 0000000 = flags

        return false;
    }

}
