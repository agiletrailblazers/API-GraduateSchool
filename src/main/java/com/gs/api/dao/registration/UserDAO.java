package com.gs.api.dao.registration;

import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.registration.User;

import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

@Repository
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall userInsertActor;
    private SimpleJdbcCall profileInsertActor;
    private SimpleJdbcCall listEntryActor;
    private SimpleJdbcCall deleteUserActor;

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

    @Value("${sql.user.single.query}")
    private String sqlForSingleUser;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertUserStoredProcedureName);
        this.profileInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertProfileStoredProcedureName);
        this.listEntryActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertfgtListEntryStoredProcedureName);
        this.deleteUserActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(deleteUserStoredProcedureName);
    }
    /**
     * Get User details for a specified user
     * @param userId the id of the user
     * @return the user details
     */
    public User getUser(String userId) {
        logger.debug("Getting user {}", userId);
        logger.debug(sqlForSingleUser);
        final User user = this.jdbcTemplate.queryForObject(sqlForSingleUser, new Object[] { userId },
                new UserRowMapper());
        logger.debug("Found session for session id {}", userId);
        return user;
    }

    /**
     *
     * @param userId the id of the user to be deleted.
     * @throws Exception error deleting user.
     */
    public boolean deleteUser(String userId) throws Exception {
        long millis = new Date().getTime();

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", userId, OracleTypes.FIXED_CHAR)
                .addValue("xts", millis, OracleTypes.VARCHAR);

        logger.debug("Deleting user. Executing stored procedure: {}", deleteUserStoredProcedureName);

        Map<String, Object> deleteUserResults = executeUserStoredProcedure(in, deleteUserActor);

        //TODO get actual success/fail result
        return true;
    }

    /**
     *
     * @param user the user information for the user to be created.
     * @return the id of the newley created user.
     * @throws Exception error creating user.
     */
    public String insertNewUser(final User user) throws Exception {
        Person person = user.getPerson();
        Address sabaFormattedAddress = mapAddressToSabaFormat(person.getPrimaryAddress());

        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        //Defaults to use until we figure out something else
        String createdByName = "sabaguest"; // hardcode default saba user till we decide otherwise
        String createdById = "emplo000000000000000";
        String split = "domin000000000000001";
        String homeDomain = "domin000000000000001"; //not sure why this is the same as above but different fields in DB
        String currency = "crncy000000000000167"; //We could execute a query to find active currency in system but this is standard
        if(user.getTimezoneId() == null) {
            user.setTimezoneId("tzone000000000000007"); //default timezone to make SP function till API is implemented
        }

        //Generate person id
        String personId = "persn" + (String) this.jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class);

        //Convert dates to sql dates
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date parsed = format.parse("20110210");
        java.sql.Date sqlDateOfBirth = new java.sql.Date(parsed.getTime());
        logger.debug("dob is {} sqlDate dob is {}", person.getDateOfBirth(), sqlDateOfBirth);

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", personId, OracleTypes.CHAR)
                .addValue("xusername", user.getUsername(), OracleTypes.VARCHAR)
                .addValue("xfname", person.getFirstName(), OracleTypes.VARCHAR)
                .addValue("xmname", person.getMiddleName(), OracleTypes.VARCHAR)
                .addValue("xlname", person.getLastName(), OracleTypes.VARCHAR)
                .addValue("xemail", person.getEmailAddress(), OracleTypes.VARCHAR)
                .addValue("xcustom2", "false", OracleTypes.VARCHAR) //We are unsure what this represents
                .addValue("xdate_of_birth", sqlDateOfBirth, OracleTypes.DATE)
                .addValue("xcustom9", person.getVeteran(), OracleTypes.VARCHAR)
                .addValue("xaddr1", sabaFormattedAddress.getAddress1(), OracleTypes.VARCHAR)
                .addValue("xaddr2", sabaFormattedAddress.getAddress2(), OracleTypes.VARCHAR)
                .addValue("xaddr3", sabaFormattedAddress.getAddress3(), OracleTypes.VARCHAR)
                .addValue("xcity", sabaFormattedAddress.getCity(), OracleTypes.VARCHAR)
                .addValue("xstate", sabaFormattedAddress.getState(), OracleTypes.VARCHAR)
                .addValue("xzip", sabaFormattedAddress.getPostalCode(), OracleTypes.VARCHAR)
                .addValue("xhomephone", person.getPrimaryPhone(), OracleTypes.VARCHAR)
                .addValue("xworkphone", person.getSecondaryPhone(), OracleTypes.VARCHAR)
                .addValue("xpassword", user.getPassword(), OracleTypes.VARCHAR)
                .addValue("xtimezone_id", user.getTimezoneId(), OracleTypes.FIXED_CHAR)
                .addValue("xcreated_on", currentDate, OracleTypes.DATE)
                .addValue("xcreated_by", createdByName, OracleTypes.VARCHAR)
                .addValue("xupdated_on", currentDate, OracleTypes.DATE)
                .addValue("xupdated_by", createdByName, OracleTypes.VARCHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                .addValue("xgender", "2", OracleTypes.FIXED_CHAR)
                .addValue("xcorres_pref1", "1", OracleTypes.FIXED_CHAR)
                .addValue("xcorres_pref2", "0", OracleTypes.FIXED_CHAR)
                .addValue("xcorres_pref3", "0", OracleTypes.FIXED_CHAR)
                .addValue("xsplit", split, OracleTypes.VARCHAR)
                .addValue("xhome_domain", homeDomain, OracleTypes.FIXED_CHAR)
                .addValue("xcurrency_id", currency, OracleTypes.FIXED_CHAR)
                //Insert lots of nulls
                .addValue("xaccount_no", null, OracleTypes.VARCHAR)
                .addValue("xcompany_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xcountry", null, OracleTypes.VARCHAR)
                .addValue("xcustom0", null, OracleTypes.VARCHAR)
                .addValue("xcustom1", null, OracleTypes.VARCHAR)
                .addValue("xcustom3", null, OracleTypes.VARCHAR)
                .addValue("xcustom4", null, OracleTypes.VARCHAR)
                .addValue("xcustom5", null, OracleTypes.VARCHAR)
                .addValue("xcustom6", null, OracleTypes.VARCHAR)
                .addValue("xcustom7", null, OracleTypes.VARCHAR)
                .addValue("xcustom8", null, OracleTypes.VARCHAR)
                .addValue("xdes_jbtyp_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xethnicity", null, OracleTypes.VARCHAR)
                .addValue("xfax", null, OracleTypes.VARCHAR)
                .addValue("xjob_title", null, OracleTypes.VARCHAR)
                .addValue("xjobtype_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xlocale_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xlocation_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xmanager_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xpassword_changed", null, OracleTypes.FIXED_CHAR)
                .addValue("xperson_no", null, OracleTypes.VARCHAR)
                .addValue("xperson_type", null, OracleTypes.VARCHAR)
                .addValue("xreligion", null, OracleTypes.VARCHAR)
                .addValue("xsecret_answer", null, OracleTypes.VARCHAR)
                .addValue("xsecret_question", null, OracleTypes.VARCHAR)
                .addValue("xss_no", null, OracleTypes.VARCHAR)
                .addValue("xstarted_on", null, OracleTypes.DATE)
                .addValue("xstatus", null, OracleTypes.VARCHAR)
                .addValue("xsuffix", null, OracleTypes.VARCHAR)
                .addValue("xterminated_on", null, OracleTypes.DATE)
                .addValue("xtitle", null, OracleTypes.VARCHAR)
                .addValue("xts", null, OracleTypes.VARCHAR);

        logger.debug("Inserting new user. Executing stored procedure: {}", insertUserStoredProcedureName);

        //TODO Can this  be wrapped in a transaction, if the later queries fail roll back the first
        Map<String, Object> insertUserResults = executeUserStoredProcedure(in, userInsertActor);

        Map<String, Object> insertProfileResults = insertUserProfile(createdByName, createdById, personId,
                split);

        String desktopPermissionListID = "listl000000000000101"; //External security role grants permission to main saba app
        String domainIDcprivIDListID = "listl000000000001004"; // Concat of two security tables ids [domin000000000000001][cpriv000000000000117]

        insertListEntry(personId, desktopPermissionListID);
        insertListEntry(personId, domainIDcprivIDListID);

        return personId;
    }

    private Map<String, Object> insertUserProfile(String createdBy, String createdById, String personId, String split) throws Exception {
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        String locale = "local000000000000001"; //This SP doesn't automatically generate this even though person_ins does
        String entryTypeId = "ppetp000000000000018"; //This is a guess, it may be a different lookup but all active rows have this

        String flags = "0010000000"; //active flag
        String profileId = "ppcor" +  (String) this.jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class);

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", profileId, OracleTypes.CHAR)
                .addValue("xcreated_on", currentDate, OracleTypes.DATE)
                .addValue("xcreated_by", createdBy, OracleTypes.VARCHAR)
                .addValue("xcreated_id", createdById, OracleTypes.CHAR)
                .addValue("xupdated_on", currentDate, OracleTypes.DATE)
                .addValue("xupdated_by", createdBy, OracleTypes.VARCHAR)
                .addValue("xtime_stamp", millis, OracleTypes.VARCHAR)
                .addValue("xentry_type_id", entryTypeId, OracleTypes.VARCHAR)
                .addValue("xprofiled_id", personId, OracleTypes.VARCHAR)
                .addValue("xlocale_id", locale, OracleTypes.CHAR)
                .addValue("xflags", flags, OracleTypes.CHAR)
                .addValue("xsplit", split, OracleTypes.CHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                //Insert lots of nulls
                .addValue("xcustom0", null, OracleTypes.VARCHAR)
                .addValue("xcustom1", null, OracleTypes.VARCHAR)
                .addValue("xcustom2", null, OracleTypes.VARCHAR)
                .addValue("xcustom3", null, OracleTypes.VARCHAR)
                .addValue("xcustom4", null, OracleTypes.VARCHAR)
                .addValue("xcustom5", null, OracleTypes.VARCHAR)
                .addValue("xcustom6", null, OracleTypes.VARCHAR)
                .addValue("xcustom7", null, OracleTypes.VARCHAR)
                .addValue("xcustom8", null, OracleTypes.VARCHAR)
                .addValue("xcustom9", null, OracleTypes.VARCHAR)
                .addValue("xjob_type_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xorganization_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xlocation_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xperson_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xsyslov1_id", null, OracleTypes.VARCHAR)
                .addValue("xsyslov2_id", null, OracleTypes.VARCHAR)
                .addValue("xsyslov3_id", null, OracleTypes.VARCHAR)
                .addValue("xsyslov4_id", null, OracleTypes.VARCHAR)
                .addValue("xsyslov5_id", null, OracleTypes.VARCHAR)
                .addValue("xuserlov1_id", null, OracleTypes.VARCHAR)
                .addValue("xuserlov2_id", null, OracleTypes.VARCHAR)
                .addValue("xuserlov3_id", null, OracleTypes.VARCHAR)
                .addValue("xuserlov4_id", null, OracleTypes.VARCHAR)
                .addValue("xuserlov5_id", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_1", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_2", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_3", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_4", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_5", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_6", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_7", null, OracleTypes.VARCHAR)
                .addValue("xlongtext0_8", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_1", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_2", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_3", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_4", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_5", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_6", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_7", null, OracleTypes.VARCHAR)
                .addValue("xlongtext1_8", null, OracleTypes.VARCHAR)
                .addValue("xtext1", null, OracleTypes.VARCHAR)
                .addValue("xtext2", null, OracleTypes.VARCHAR)
                .addValue("xtext3", null, OracleTypes.VARCHAR)
                .addValue("xtext4", null, OracleTypes.VARCHAR)
                .addValue("xtext5", null, OracleTypes.VARCHAR)
                .addValue("xdate1", null, OracleTypes.DATE)
                .addValue("xdate2", null, OracleTypes.DATE)
                .addValue("xdate3", null, OracleTypes.DATE)
                .addValue("xint1", null, OracleTypes.INTEGER)
                .addValue("xint2", null, OracleTypes.INTEGER)
                .addValue("xint3", null, OracleTypes.INTEGER);

        logger.debug("Inserting user profile. Executing stored procedure: {}", insertProfileStoredProcedureName);

        /*TODO This results in one row in the profile table with an active flag. However, Saba inserts 6 additional rows,
        the only difference is entry_type_id and the acronym in the profile id. Might need to execute additional times*/

        return executeUserStoredProcedure(in, profileInsertActor);
    }

    private Map<String, Object> insertListEntry(String personId, String listId) throws Exception {
        long millis = new Date().getTime();

        String listEntryId = "liste" +  this.jdbcTemplate.queryForObject(getListEntryIdSequenceQuery, String.class);

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", listEntryId, OracleTypes.FIXED_CHAR)
                .addValue("xts", null, OracleTypes.VARCHAR)
                .addValue("xperson_id", personId, OracleTypes.FIXED_CHAR)
                .addValue("xlist_id", listId, OracleTypes.FIXED_CHAR)
                .addValue("xgroup_label", null, OracleTypes.VARCHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR);

        logger.debug("Inserting user profile. Executing stored procedure: {}", insertfgtListEntryStoredProcedureName);
        return executeUserStoredProcedure(in, listEntryActor);
    }

    private Map<String,Object> executeUserStoredProcedure(SqlParameterSource inParameters, SimpleJdbcCall spCallToExecute) throws Exception {
        Map<String,Object> out = spCallToExecute.execute(inParameters);

        logger.debug("Stored Procedure executed successfully");
        return out;
    }

    /**
     * In SABA, when entering a new user, address1 = Office/Dept, address2 = Ste/Mail Stop, and address3 = Street/PO Box
     * The new database will use address1 = Street/PO Box, then additional optional address2 and address3 fields
     * @param addressToChange the address formatted for new database
     * @return the Address formatted for SABA
     */
    private Address mapAddressToSabaFormat(Address addressToChange) {
        Address sabaFormattedAddress = new Address();
        sabaFormattedAddress.setId(addressToChange.getId());
        sabaFormattedAddress.setAddress1(addressToChange.getAddress2());
        sabaFormattedAddress.setAddress2(addressToChange.getAddress3());
        sabaFormattedAddress.setAddress3(addressToChange.getAddress1());
        sabaFormattedAddress.setCity(addressToChange.getCity());
        sabaFormattedAddress.setState(addressToChange.getState());
        sabaFormattedAddress.setPostalCode(addressToChange.getPostalCode());

        return sabaFormattedAddress;
    }

    /**
     * Maps a user result to a User object
     */
    protected final class UserRowMapper implements RowMapper<User> {
        /**
         * Map row for Course object from result set
         */
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getString("USER_ID"));
            user.setUsername(rs.getString("USERNAME"));
            String ssn = rs.getString("SS_NO");
            if (ssn != null && ssn.length() > 4) {
                user.setLastFourSSN(ssn.substring(ssn.length() - 4, ssn.length()));
            }
            user.setTimezoneId(rs.getString("TIMEZONE_ID"));
            user.setAccountId(rs.getString("ACCOUNT_ID"));
            user.setSplit(rs.getString("SPLIT"));
            user.setCurrencyId(rs.getString("CURRENCY_ID"));

            Person person = new Person();
            person.setFirstName(rs.getString("FNAME"));
            person.setLastName(rs.getString("LNAME"));
            person.setEmailAddress(rs.getString("EMAIL"));
            person.setPrimaryPhone(rs.getString("HOMEPHONE"));
            person.setSecondaryPhone(rs.getString("WORKPHONE"));
            if (rs.getDate("DATE_OF_BIRTH") != null) {
                person.setDateOfBirth(rs.getDate("DATE_OF_BIRTH").toString());
            }
            String veteranStatus = rs.getString("VETERAN");
            if (veteranStatus != null) {
                //Translate y/Y and n/N to true false
                if (veteranStatus.equals("y") || veteranStatus.equals("Y")) {
                    person.setVeteran(true);
                } else if (veteranStatus.equals("n") || veteranStatus.equals("N")) {
                    person.setVeteran(false);
                }
            }
            user.setPerson(person);

            return user;
        }
    }
}