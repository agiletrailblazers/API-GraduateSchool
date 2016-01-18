package com.gs.api.dao.registration;

import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.registration.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

@Repository
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall personActor;

    private static final String insertStoredProceudreName = "tpp_person_ins";
    private static final String insertProfileStoredProceudreName = "cmp_profile_entry_ins";
    private static final String getPersIdSequenceQuery = "select lpad(ltrim(rtrim(to_char(tpt_person_seq.nextval))), 15, '0') id from dual";
    private static final String getProfileIdSequenceQuery = "select lpad(ltrim(rtrim(to_char(cmt_profile_entry_seq.nextval))), 15, '0') id from dual";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.personActor = new SimpleJdbcCall(this.jdbcTemplate);
    }

    /**
     * Get user object from database
     * @param personId person id for specified user
     * @return specified User
     */
    public User getUserById(String personId) {
        logger.debug("Getting user for ID {}", personId);
        try {
            User user = new User();
            //this.jdbcTemplate.query("stub");
            logger.debug("Found user");
            return user;
        }
        catch (EmptyResultDataAccessException e) {
            logger.warn("User not found - {}", e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving account - {}", e);
            throw e;
        }
    }

    /**
     * Get user object from database
     * @param personNo person number for specified user
     * @return specified User
     */
    public User getUserByPersonNo(String personNo) {
        logger.debug("Getting user for person number {}", personNo);
        try {
            User user = new User();
            //this.jdbcTemplate.query("stub");
            logger.debug("Found user");
            return user;
        }
        catch (EmptyResultDataAccessException e) {
            logger.warn("User not found - {}", e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving account - {}", e);
            throw e;
        }
    }

    /**
     * Get user object from database
     * @param username username for specified user
     * @return specified User
     */
    public User getUserByUsername(String username) {
        logger.debug("Getting user for username {}", username);
        try {
            User user = new User();
            //this.jdbcTemplate.query("stub");
            logger.debug("Found user");
            return user;
        }
        catch (EmptyResultDataAccessException e) {
            logger.warn("User not found - {}", e);
            return null;
        }
        catch (Exception e) {
            logger.error("Error retrieving account - {}", e);
            throw e;
        }
    }

    /**
     *
     * @param user the user information for the user to be created.
     * @return the id of the newley created user.
     * @throws Exception error creating user.
     */
    public String insertNewUser(final User user) throws Exception {
        Person person = user.getPerson();
        Address address = person.getPrimaryAddress();
        return insertNewUser(user.getUsername(), person.getFirstName(), person.getMiddleName(), person.getLastName(),
                person.getVeteran(), null, address.getAddress2(), address.getAddress1(), address.getCity(), address.getState(),
                address.getPostalCode(), person.getPrimaryPhone(), null, user.getPassword(), user.getTimezoneId(),
                person.getEmailAddress());
    }

    /**
     * Insert user into the database
     * @return Return the unique id of the user that was created.
     */
    private String insertNewUser(String username, String firstName, String middleName, String lastName, boolean veteranStatus,
                                 String address1Office, String address1SteMailStop, String address1StreetPoBox, String address1City,
                                 String address1State, String address1Zip, String primaryPhone, String secondaryPhone,
                                 String password, String timeZone, String email) throws Exception {
        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        //Use until we figure out something else
        String createdByName = "sabaguest"; // hardcode default saba user till we decide otherwise
        String createdById = "emplo000000000000000";
        String split = "domin000000000000001";
        String homeDomain = "domin000000000000001"; //not sure why this is the same as above but different fields in DB
        String currency = "crncy000000000000167"; //We could execute a query to find active currency in system but this is standard

        //Generate person id
        String personId = "persn" + (String) this.jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class);

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", personId)
                .addValue("xusername", username)
                .addValue("xfname", firstName)
                .addValue("xmname", middleName)
                .addValue("xlname", lastName)
                .addValue("xemail", email)
                .addValue("custom2", veteranStatus)
                .addValue("xaddr1", address1Office)
                .addValue("xaddr2", address1SteMailStop)
                .addValue("xaddr3", address1StreetPoBox)
                .addValue("xcity", address1City)
                .addValue("xstate", address1State) //TODO this must be a two letter state
                .addValue("xzip", address1Zip)
                .addValue("xhomephone", primaryPhone)
                .addValue("xworkphone", secondaryPhone)
                .addValue("xpassword", password)
                .addValue("xtimezone_id", timeZone)
                .addValue("xcreated_on", currentDate)
                .addValue("xcreated_by", createdByName)
                .addValue("xupdated_on", currentDate)
                .addValue("xupdated_by", createdByName)
                .addValue("xnewts", millis)
                .addValue("xgender", '2')
                .addValue("xcorres_pref1", '1')
                .addValue("xcorres_pref2", '0')
                .addValue("xcorres_pref3", '0')
                .addValue("xsplit", split)
                .addValue("xhome_domain", homeDomain)
                .addValue("xcurrency_id", currency);

        logger.debug("Inserting new user");

//        Map<String, Object> insertUserResults = executeUserStoredProcedure(in, insertStoredProceudreName);
//
//        Map<String, Object> insertProfileResults = insertUserProfile(createdByName, createdById, "resulting person id from above",
//                split);

        return personId;
    }

    private Map<String, Object> insertUserProfile(String createdBy, String createdById, String personId, String split) throws Exception {
        Date currentDate = new Date();
        long millis = new Date().getTime();

        String locale = "local000000000000001"; //This SP doesn't automatically generate this even though person_ins does
        String entryTypeId = "ppetp000000000000018"; //This is a guess, it may be a different lookup but all active rows have this

        String flags = "0010000000"; //active flag
        String profileId = "ppcor" +  (String) this.jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class);

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", profileId)
                .addValue("xcreated_on", currentDate)
                .addValue("xcreated_by", createdBy)
                .addValue("xcreated_id", createdById)
                .addValue("xupdated_on", currentDate)
                .addValue("xupdated_by", createdBy)
                .addValue("xtime_stamp", millis)
                .addValue("xentry_type_id", entryTypeId)
                .addValue("xprofiled_id", personId)
                .addValue("xlocale_id", locale)
                .addValue("xflags", flags)
                .addValue("xsplit", split);

        logger.debug("Inserting user profile");
        return executeUserStoredProcedure(in, insertProfileStoredProceudreName);
    }

    private Map<String,Object> executeUserStoredProcedure(SqlParameterSource inParameters, String procedureToExecute) throws Exception {
        try {
            logger.debug("Executing stored procedure ", procedureToExecute);

            personActor.withProcedureName(procedureToExecute);
            Map<String,Object> out = personActor.execute(inParameters);

            logger.debug("Stored Procedure {} executed successfully", procedureToExecute);
            return out;
        }
        catch (Exception e) {
            logger.error("Error inserting user - {}", e);
            throw e;
        }
    }
}
