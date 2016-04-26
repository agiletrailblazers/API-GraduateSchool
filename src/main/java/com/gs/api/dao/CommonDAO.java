package com.gs.api.dao;

import com.gs.api.domain.registration.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CommonDAO {
    private static final Logger logger = LoggerFactory.getLogger(CommonDAO.class);
    static final String SABA_GUEST = "sabaguest";
    static final String SABA_GUEST_ID = "emplo000000000000000";
    static final String SABA_ADMIN_ID = "emplo000000000000001";
    private JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall userInsertActor;
    private SimpleJdbcCall profileInsertActor;
    private SimpleJdbcCall listEntryActor;
    private SimpleJdbcCall deleteUserActor;
    private SimpleJdbcCall resetPasswordActor;

    @Value("${sql.user.personInsert.procedure}")
    private String insertUserStoredProcedureName;
    @Value("${sql.user.profileInsert.procedure}")
    private String insertProfileStoredProcedureName;
    @Value("${sql.user.listEntryInsert.procedure}")
    private String insertfgtListEntryStoredProcedureName;
    @Value("${sql.user.deleteUser.procedure}")
    private String deleteUserStoredProcedureName;

    @Value("${sql.user.personId.sequence}")
    private String getPersIdSequenceQuery;
    @Value("${sql.user.profileId.sequence}")
    private String getProfileIdSequenceQuery;
    @Value("${sql.user.listEntry.sequence}")
    private String getListEntryIdSequenceQuery;

    @Value("${sql.user.login.query}")
    private String sqlForUserLogin;

    @Value("${sql.user.single.query}")
    private String sqlForSingleUser;

    @Value("${sql.user.username.query}")
    private String sqlForUserByUsername;

    @Value("${sql.user.timezones.query}")
    private String sqlForTimezones;

    @Value("${sql.user.resetPassword.procedure}")
    private String resetPasswordStoredProcedureName;

    @Value("${sql.user.password.query}")
    private String sqlForPasswordQuery;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertUserStoredProcedureName);
        this.profileInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertProfileStoredProcedureName);
        this.listEntryActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertfgtListEntryStoredProcedureName);
        this.deleteUserActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(deleteUserStoredProcedureName);
        this.resetPasswordActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(resetPasswordStoredProcedureName);
    }

    /**
     * Get the list of timezones
     * @return list of timezones
     * @throws Exception
     */
    public List<Timezone> getTimezones() throws Exception {
        logger.debug("Getting list of timezones - " + sqlForTimezones);

        final List<Timezone> timezones = this.jdbcTemplate.query(sqlForTimezones, new TimezoneRowMapper());
        logger.debug("Found {} timezones", timezones.size());
        return timezones;
    }

    protected final class TimezoneRowMapper implements RowMapper<Timezone> {
        /**
         * Map a row for a Timezone object from a result set
         */
        public Timezone mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timezone timezone = new Timezone();
            timezone.setId(rs.getString("ID"));
            timezone.setName(rs.getString("NAME"));
            return timezone;
        }
    }

}
