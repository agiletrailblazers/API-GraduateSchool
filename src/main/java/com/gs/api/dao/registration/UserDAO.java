package com.gs.api.dao.registration;

import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

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

@Repository
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall personInsertActor;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.personInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("tpp_person_ins");
    }

    public Map<String,Object> insertNewUser(String usernameEmail, String firstName, String middleName, String lastName, boolean veteranStatus,
                                 String address1Office, String address1SteMailStop, String address1StreetPoBox, String address1City,
                                 String address1State, String address1Zip, String address2Office, String address2SteMailStop,
                                 String address2StreetPoBox, String address2City, String address2State, String address2Zip,
                                 String primaryPhone, String secondaryPhone, String password, String timeZone) throws Exception {

        Date currentDate = new Date();
        String createdByName = "RegistrationSystem";
        long millis = new Date().getTime();

        SqlParameterSource in = new MapSqlParameterSource()
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
        return executeInsertStoredProcedure(in);
    }

    public Map<String,Object> insertNewUserFull(char id, String ts, String title, String personNo, String firstName, String lastName,
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
        return executeInsertStoredProcedure(in);
    }

    public User getUserById(String accountId) {
        logger.debug("Getting account for ID {}", accountId);
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

    public boolean updateUser() {
        //stub - need to investigate update procedure
        return false;
    }

    public void updateUserProfile() {


    }

    private Map<String,Object> executeInsertStoredProcedure(SqlParameterSource inParameters) throws Exception {
        try {
            logger.debug(jdbcTemplate.toString());
            logger.debug(personInsertActor.toString());
            Map<String,Object> out = personInsertActor.execute(inParameters);
            logger.debug("New user successfully added");
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

            Map<String, Object> actualResults = userDAO.insertNewUser("test@test.gov", "firstName", "middle",
                    "lastName", false, "Test Office", "123 Test Street", null, "testCity", "MD", "12345", null, null, null,
                    null, null, null, "1234561234", null, "password1", null);
        }
        catch (Exception E) {
            logger.error("This didn't work");
        }


    }
}
