package com.gs.api.dao.registration;

import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.registration.Timezone;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.DuplicateUserException;
import com.gs.api.exception.ReusedPasswordException;
import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    static final String SABA_ADMIN_ID = "emplo000000000000001";
    static final String SABA_GUEST = "sabaguest";
    static final String SABA_GUEST_ID = "emplo000000000000000";
    static final String SPLIT_ID = "domin000000000000001"; //Seems to be an alias for domain but different fields in DB
    static final String HOME_DOMAIN_ID = "domin000000000000001"; //Seems to be an alias for split but different fields in DB
    static final String CURRENCY_ID = "crncy000000000000167"; //We could execute a query to find active currency in system but this is standard
    static final String DESKTOP_PERMISSION_LIST_ID = "listl000000000000101"; //External security role grants permission to main saba app
    static final String DOMAIN_ID_CPRIV_ID_LIST_ID = "listl000000000001004"; // Concat of two security tables ids [domin000000000000001][cpriv000000000000117]
    static final String LOCALE_ID = "local000000000000001"; //Some SP automatically generate this but person_ins does not so it must be specified
    static final String ENTRY_TYPE_ID = "ppetp000000000000018"; //This is a guess, it may be a different lookup but all active rows have this

    private JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall userInsertActor;
    private SimpleJdbcCall profileInsertActor;
    private SimpleJdbcCall listEntryActor;
    private SimpleJdbcCall deleteUserActor;
    private SimpleJdbcCall changePasswordActor;

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

    @Value("${sql.user.changePassword.procedure}")
    private String changePasswordStoredProcedureName;

    @Value("${sql.user.password.query}")
    private String sqlForPasswordQuery;

    @Value("${sql.user.needPWChange.query}")
    private String sqlForNeedPWChangeCheck;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertUserStoredProcedureName);
        this.profileInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertProfileStoredProcedureName);
        this.listEntryActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertfgtListEntryStoredProcedureName);
        this.deleteUserActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(deleteUserStoredProcedureName);
        this.changePasswordActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(changePasswordStoredProcedureName);
    }

    /**
     * Get User details for a specified user
     * @param userId the id of the user
     * @return the user details or null if no user found matching the supplied id
     */
    public User getUser(String userId) {
        logger.debug("Getting user {}", userId);
        try {
            User user = this.jdbcTemplate.queryForObject(sqlForSingleUser, new Object[]{userId},
                    new UserRowMapper());
            logger.debug("Found user for user id {}", userId);
            return user;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            logger.debug("Expected 1 user with id {} and supplied password, found {}", userId, e.getActualSize());
            return null;
        }
    }

    /**
     * Get user details by the username and password
     * @param username the username
     * @param password the encrypted password
     * @return the user or null if no user found matching the supplied username and password
     */
    public User getUser(String username, String password) {
        logger.debug("Getting user by username {} and supplied password", username);
        try {
            User user = this.jdbcTemplate.queryForObject(sqlForUserLogin, new Object[] { username, password },
                    new UserRowMapper());

            logger.debug("Found user with username {} and supplied password", username);
            return user;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            logger.debug("Expected 1 user with username {} and supplied password, found {}", username, e.getActualSize());
            return null;
        }
    }

    /**
     * Deletes specified user. Will only work if the user is not registered for any courses
     * @param userId the id of the user to be deleted.
     * @param userTimestamp the timestamp associated with the user id.
     * @throws Exception error deleting user.
     */
    public boolean deleteUser(String userId, String userTimestamp) throws Exception {

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", userId, OracleTypes.FIXED_CHAR)
                .addValue("xts", userTimestamp, OracleTypes.VARCHAR);

        logger.debug("Deleting user. Executing stored procedure: {}", deleteUserStoredProcedureName);

        executeUserStoredProcedure(in, deleteUserActor);

        // throws sql exception for failure
        return true;
    }

    /**
     * Get user details by the username
     * @param username the username
     * @return the user or null if no user found matching the supplied username
     */
    public User getUserByUsername(String username) {

        logger.debug("Getting user by username {}", username);
        try {
            User user = this.jdbcTemplate.queryForObject(sqlForUserByUsername, new Object[] { username },
                    new UserRowMapper());

            logger.debug("Found user with username {}", username);
            return user;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            logger.debug("Expected 1 user with username {}, found {}", username, e.getActualSize());
            return null;
        }
    }

    public void resetForgottenPassword(String userId, String newPassword) throws Exception {

        logger.debug("Reset forgotten password for user {}, looking up current password", userId);

        // lookup the current password, it is needed to reset the password
        String currentPassword = this.jdbcTemplate.queryForObject(sqlForPasswordQuery, new Object[]{userId}, String.class);

        logger.debug("Found password for user {}", userId);

        // reset the password using the saba admin user id and the users current password
        changePassword(userId, SABA_ADMIN_ID, currentPassword, newPassword);
    }

    public void changeUserPassword(String userId, String currentPassword, String newPassword) throws Exception {

        logger.debug("Changing password for user {}", userId);

        // change the password using the saba admin user id and the users current password
        changePassword(userId, userId, currentPassword, newPassword);
    }

    /**
     * Executes all procedures to insert a new user into the database
     * @param user the user information for the user to be created.
     * @return the id of the newley created user.
     * @throws Exception error creating user.
     */
    @Transactional("transactionManager")
    public String createUser(final User user) throws Exception {
        User existingUser = getUserByUsername(user.getUsername());
        if (existingUser != null) {
            String errorString = "User "+ user.getUsername() + " already exists";
            logger.debug(errorString);
            throw new DuplicateUserException(errorString);
        }

        String userId = insertNewUser(user);

        insertUserProfile(userId);

        insertListEntry(userId, DESKTOP_PERMISSION_LIST_ID);
        insertListEntry(userId, DOMAIN_ID_CPRIV_ID_LIST_ID);
        return userId;
    }

    /**
     * Inserts the person into the database
     * @param user the user information for the user to be created.
     * @return the id of the newley created user.
     * @throws Exception error creating user.
     */
    private String insertNewUser(final User user) throws Exception {
        Person person = user.getPerson();
        Address sabaFormattedAddress = mapAddressToSabaFormat(person.getPrimaryAddress());

        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        //Generate person id
        String personId = "persn" + this.jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class);

        //Convert dates to sql dates
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date parsed = format.parse(person.getDateOfBirth());
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
                .addValue("xcreated_by", SABA_GUEST, OracleTypes.VARCHAR)
                .addValue("xupdated_on", currentDate, OracleTypes.DATE)
                .addValue("xupdated_by", SABA_GUEST, OracleTypes.VARCHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                //Hardcoded values till we figure out something better
                .addValue("xgender", "2", OracleTypes.FIXED_CHAR)
                .addValue("xcorres_pref1", "1", OracleTypes.FIXED_CHAR)
                .addValue("xcorres_pref2", "0", OracleTypes.FIXED_CHAR)
                .addValue("xcorres_pref3", "0", OracleTypes.FIXED_CHAR)
                .addValue("xsplit", SPLIT_ID, OracleTypes.VARCHAR)
                .addValue("xhome_domain", HOME_DOMAIN_ID, OracleTypes.FIXED_CHAR)
                .addValue("xcurrency_id", CURRENCY_ID, OracleTypes.FIXED_CHAR)
                //Null values must be inserted for all other parameters
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
        executeUserStoredProcedure(in, userInsertActor);

        return personId;
    }

    /**
     * Inserts information about the user's profile
     * Note: insertUserProfile results in one row in the profile table with an active flag. However, Saba inserts
     * 6 other rows with different entry_type_id and the acronym in the profile id. Might need to execute additional times
     * @param personId created person id
     * @throws Exception
     */
    private void insertUserProfile(String personId) throws Exception {
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        String profileId = "ppcor" +  this.jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class);

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", profileId, OracleTypes.CHAR)
                .addValue("xcreated_on", currentDate, OracleTypes.DATE)
                .addValue("xcreated_by", SABA_GUEST, OracleTypes.VARCHAR)
                .addValue("xcreated_id", SABA_GUEST_ID, OracleTypes.CHAR)
                .addValue("xupdated_on", currentDate, OracleTypes.DATE)
                .addValue("xupdated_by", SABA_GUEST, OracleTypes.VARCHAR)
                .addValue("xtime_stamp", millis, OracleTypes.VARCHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                .addValue("xprofiled_id", personId, OracleTypes.VARCHAR)
                .addValue("xflags", "0010000000", OracleTypes.CHAR) //active flag
                //Hardcoded values till we figure out something better
                .addValue("xentry_type_id", ENTRY_TYPE_ID, OracleTypes.VARCHAR)
                .addValue("xlocale_id", LOCALE_ID, OracleTypes.CHAR)
                .addValue("xsplit", SPLIT_ID, OracleTypes.CHAR)
                //Null values must be inserted for all other parameters
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

        executeUserStoredProcedure(in, profileInsertActor);
    }

    /**
     * Inserts security information about the user. May be called multiple times with different security lists
     * @param personId the newly created user
     * @param listId the list which the user is being added to
     * @return the results of the stored procedure
     * @throws Exception
     */
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

        logger.debug("Inserting user list entry for user. Executing stored procedure: {}", insertfgtListEntryStoredProcedureName);
        return executeUserStoredProcedure(in, listEntryActor);
    }

    private Map<String,Object> executeUserStoredProcedure(SqlParameterSource inParameters, SimpleJdbcCall spCallToExecute) throws Exception {
        Map<String,Object> out = spCallToExecute.execute(inParameters);

        logger.debug("Stored Procedure executed successfully");
        return out;
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
            if (ssn != null) {
                if (ssn.length() > 4) {
                    user.setLastFourSSN(ssn.substring(ssn.length() - 4, ssn.length()));
                }
                else if (ssn.length() == 4) {
                    user.setLastFourSSN(ssn);
                }
                else {
                    user.setLastFourSSN(null);
                }
            }
            user.setTimezoneId(rs.getString("TIMEZONE_ID"));
            user.setAccountId(rs.getString("ACCOUNT_ID"));
            user.setSplit(rs.getString("SPLIT"));
            user.setCurrencyId(rs.getString("CURRENCY_ID"));
            user.setTimestamp(rs.getString("TIME_STAMP"));

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
                if (veteranStatus.equalsIgnoreCase("y")) {
                    person.setVeteran(true);
                } else if (veteranStatus.equalsIgnoreCase("n")) {
                    person.setVeteran(false);
                }
            }

            Address address = new Address();
            address.setAddress1(rs.getString("ADDRESS1"));
            address.setAddress2(rs.getString("ADDRESS2"));
            address.setCity(rs.getString("CITY"));
            address.setState(rs.getString("STATE"));
            address.setPostalCode(rs.getString("ZIP"));

            person.setPrimaryAddress(address);
            user.setPerson(person);

            return user;
        }
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

    private void changePassword(String userId, String currentUserId, String oldPassword, String newPassword) throws Exception {

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", userId, OracleTypes.CHAR)
                .addValue("xold_password", oldPassword, OracleTypes.VARCHAR)
                .addValue("xnew_password", newPassword, OracleTypes.VARCHAR)
                .addValue("xcurr_user_id", currentUserId, OracleTypes.CHAR);

        logger.debug("Resetting user password. Executing stored procedure: {}", changePasswordStoredProcedureName);
        try {
            executeUserStoredProcedure(in, changePasswordActor);
        } catch (UncategorizedSQLException e) {
            //Stored proc throws error 20958 when password is reused
            if (e.getMessage().contains("20958")){
                throw new ReusedPasswordException("Cannot reuse password", e);
            } else {
                //Rethrow exception
                throw e;
            }
        }
    }

    public boolean needsPasswordChange(String userId){
        String createdBy = this.jdbcTemplate.queryForObject(sqlForNeedPWChangeCheck, new Object[] { userId }, String.class);

        //the stored procedure 'tpp_person_ins' inserts value for newly created user
        // in tpt_password_history table with 'emp' in the createdby field
        if (!createdBy.equalsIgnoreCase(userId) && !createdBy.equalsIgnoreCase("emp")){
            return true;
        }

        return false;

    }
}
