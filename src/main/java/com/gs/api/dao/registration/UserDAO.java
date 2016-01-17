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
import java.util.TimeZone;

import javax.sql.DataSource;

@Repository
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall personActor;

    private static final String insertStoredProceudreName = "tpp_person_ins";
    private static final String insertProfileStoredProceudreName = "cmp_profile_entry_ins";

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

        /*
            TODO
              remove hard coded veteran status and timezone when/if it gets collected
              in the UI and passed back as a part of the person?
         */
        final boolean veteran = false;
        final TimeZone timezone = TimeZone.getTimeZone("US/Eastern");

        Person person = user.getPerson();
        Address address = person.getPrimaryAddress();
        return insertNewUser(user.getUsername(), person.getFirstName(), person.getMiddleName(), person.getLastName(),
                veteran, null, address.getAddress2(), address.getAddress1(), address.getCity(), address.getState(),
                address.getPostalCode(), null, null, null, null, null, null, person.getPrimaryPhone(), null,
                user.getPassword(), timezone.getID());
    }

    /**
     * Insert user into the database
     * @return Return the unique id of the user that was created.
     */
    private String insertNewUser(String usernameEmail, String firstName, String middleName, String lastName, boolean veteranStatus,
                                 String address1Office, String address1SteMailStop, String address1StreetPoBox, String address1City,
                                 String address1State, String address1Zip, String address2Office, String address2SteMailStop,
                                 String address2StreetPoBox, String address2City, String address2State, String address2Zip,
                                 String primaryPhone, String secondaryPhone, String password, String timeZone) throws Exception {

        Date currentDate = new Date();
        String createdByName = "RegistrationSystem";
        long millis = new Date().getTime();
        String id = "pers" + "someNumbers";

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", id)
                .addValue("xusername", usernameEmail)
                .addValue("xfname", firstName)
                .addValue("xmname", middleName)
                .addValue("xlname", lastName)
                //.addValue("whatever is veteran status", veteranStatus)
                .addValue("xaddr1", address1StreetPoBox)
                .addValue("xaddr2", address1SteMailStop)
                .addValue("xaddr1", address1Office)
                .addValue("xcity", address1City)
                .addValue("xstate", address1State)
                .addValue("xzip", address1Zip)
                .addValue("xhomephone", primaryPhone)
                .addValue("xworkphone", secondaryPhone)
                .addValue("xpassword", password)
                .addValue("xtimezone_id", timeZone)
                .addValue("xcreated_on", currentDate)
                .addValue("xcreated_by", createdByName)
                .addValue("xupdated_on", currentDate)
                .addValue("xupdated_by", createdByName)
                .addValue("xts", millis)
                .addValue("xnewts", millis);

        logger.debug("Inserting new user");

//        Map<String, Object> insertUserResults = executeUserStoredProcedure(in, insertStoredProceudreName);
//
//        Map<String, Object> insertProfileResults = insertUserProfile(createdByName, "createdById", "sysLov1Id", "entryTypeId", "resulting person id from above",
//                "locale id");

        return "IDofCreatedUser";
    }

    /**
     * Likely deprecated, delete if unused
     */

    private Map<String,Object> insertNewUserFull(char id, String ts, String title, String personNo, String firstName, String lastName,
                                 String middleName, String homePhone, String workPhone, String fax, String createdBy,
                                 Date createdOn, String updatedBy, Date updatedOn, String custom0, String custom1,
                                 String custom2, char companyId, String address1,String address2, String address3,
                                 String city, String state, String zip, String country, String email, String split,
                                 String custom3, String custom4, char desJbtypId, char jobTypeId, char localeId, String password,
                                 String userName, char managerId, char timeZoneId, char corres_pref1, char corres_pref2,
                                 char corres_pref3, String custom5, String custom6, String custom7, String custom8,
                                 String custom9, char currencyId, String suffix, String jobTitle, String ssNo, String status,
                                 String personType, char locationId, char homeDomain, String accountNo, char gender,
                                 Date startedOn, Date terminatedOn, Date dateOfBirth, String ethnicity, String religion,
                                 String secretQuestion, String secretAnswer, char passwordChanged, String newTs) throws Exception {
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", id)
                .addValue("xts", ts)
                .addValue("xtitle", title)
                .addValue("xperson_no", personNo)
                .addValue("xfname", firstName)
                .addValue("xlanme", lastName)
                .addValue("xmname", middleName)
                .addValue("xhomephone", homePhone)
                .addValue("xworkphone", workPhone)
                .addValue("xfax", fax)
                .addValue("xcreated_by", createdBy)
                .addValue("xcreated_on", createdOn)
                .addValue("xupdated_by", updatedBy)
                .addValue("xupdated_on", updatedOn)
                .addValue("xcustom0", custom0)
                .addValue("xcustom1", custom1)
                .addValue("xcustom2", custom2)
                .addValue("xcompany_id", companyId)
                .addValue("xaddr1", address1)
                .addValue("xaddr2", address2)
                .addValue("xaddr3", address3)
                .addValue("xcity", city)
                .addValue("xstate", state)
                .addValue("xzip", zip)
                .addValue("xcountry", country)
                .addValue("xemail", email)
                .addValue("xsplit", split)
                .addValue("xcustom3", custom3)
                .addValue("xcustom4", custom4)
                .addValue("xdes_jbtyp_id", desJbtypId)
                .addValue("xjobtype_id", jobTypeId)
                .addValue("xlocale_id", localeId)
                .addValue("xpassword", password)
                .addValue("xusername", userName)
                .addValue("xmanager_id", managerId)
                .addValue("xtimezone_id", timeZoneId)
                .addValue("xcorres_pref1", corres_pref1)
                .addValue("xcorres_pref2", corres_pref2)
                .addValue("xcorres_pref3", corres_pref3)
                .addValue("xcustom5", custom5)
                .addValue("xcustom6", custom6)
                .addValue("xcustom7", custom7)
                .addValue("xcustom8", custom8)
                .addValue("xcustom9", custom9)
                .addValue("xcurrency_id", currencyId)
                .addValue("xsuffix", suffix)
                .addValue("xjob_title", jobTitle)
                .addValue("xss_no", ssNo)
                .addValue("xstatus", status)
                .addValue("xperson_type", personType)
                .addValue("xlocation_id", locationId)
                .addValue("xhome_domain", homeDomain)
                .addValue("xaccount_no", accountNo)
                .addValue("xgender", gender)
                .addValue("xstarted_on", startedOn)
                .addValue("xterminated_on", terminatedOn)
                .addValue("xdate_of_birth", dateOfBirth)
                .addValue("xethnicity", ethnicity)
                .addValue("xreligion", religion)
                .addValue("xsecret_question", secretQuestion)
                .addValue("xsecret_answer", secretAnswer)
                .addValue("xpassword_changed", passwordChanged)
                .addValue("xnewts", newTs);

        logger.debug("Inserting user with full parameters");
        return executeUserStoredProcedure(in, insertStoredProceudreName);
    }


    private Map<String, Object> insertUserProfile(String createdBy, String createdById, String sysLov1Id, String entryTypeId,
                                  String profiledId, String localeId) throws Exception {
        Date currentDate = new Date();
        String createdByName = "RegistrationSystem";
        long millis = new Date().getTime();
        String id = "pp" + "someIdentifier" + "someNumbers";

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", id)
                .addValue("xcreated_on", currentDate)
                .addValue("xcreated_by", createdByName)
                .addValue("xcreated_id", createdById) //id of user who created
                .addValue("xupdated_on", currentDate)
                .addValue("xupdated_by", createdByName)
                .addValue("xtime_stamp", millis)
                .addValue("xsyslov1_id", sysLov1Id)
                .addValue("xentry_type_id", entryTypeId)
                .addValue("xprofiled_id", profiledId)
                .addValue("xlocale_id", localeId);


        logger.debug("Inserting user profile");
        return executeUserStoredProcedure(in, insertProfileStoredProceudreName);
    }

    private Map<String,Object> executeUserStoredProcedure(SqlParameterSource inParameters, String procedureToExecute) throws Exception {
        try {
            logger.debug(jdbcTemplate.toString()); //debug issue this is null with main below
            logger.debug(personActor.toString()); //debug issue

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

    public static void main (String[] args) {
        UserDAO userDAO = new UserDAO();
        try {

            String actualResults = userDAO.insertNewUser("test@test.gov", "firstName", "middle",
                    "lastName", false, "Test Office", "123 Test Street", null, "testCity", "MD", "12345", null, null, null,
                    null, null, null, "1234561234", null, "password1", null);
        }
        catch (Exception E) {
            logger.error("This didn't work");
        }


    }
}
