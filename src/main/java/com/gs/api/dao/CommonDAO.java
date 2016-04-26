package com.gs.api.dao;

import com.gs.api.domain.registration.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CommonDAO {
    private static final Logger logger = LoggerFactory.getLogger(CommonDAO.class);
    private JdbcTemplate jdbcTemplate;

    @Value("${sql.user.timezones.query}")
    private String sqlForTimezones;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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
