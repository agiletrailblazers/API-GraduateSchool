package com.gs.api.dao.registration;

import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.registration.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private SimpleJdbcCall userInsertActor;
    private SimpleJdbcCall profileInsertActor;

    private static final String insertUserStoredProceudreName = "tpp_person_ins";
    private static final String insertProfileStoredProceudreName = "cmp_profile_entry_ins";
    private static final String getPersIdSequenceQuery = "select lpad(ltrim(rtrim(to_char(tpt_person_seq.nextval))), 15, '0') id from dual";
    private static final String getProfileIdSequenceQuery = "select lpad(ltrim(rtrim(to_char(cmt_profile_entry_seq.nextval))), 15, '0') id from dual";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertUserStoredProceudreName);
        this.profileInsertActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertProfileStoredProceudreName);
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
                .addValue("xusername", user.getUsername())
                .addValue("xfname", person.getFirstName())
                .addValue("xmname", person.getMiddleName())
                .addValue("xlname", person.getLastName())
                .addValue("xemail", person.getEmailAddress())
                .addValue("xcustom2", person.getVeteran())
                .addValue("xaddr1", sabaFormattedAddress.getAddress1())
                .addValue("xaddr2", sabaFormattedAddress.getAddress2())
                .addValue("xaddr3", sabaFormattedAddress.getAddress3())
                .addValue("xcity", sabaFormattedAddress.getCity())
                .addValue("xstate", sabaFormattedAddress.getState())
                .addValue("xzip", sabaFormattedAddress.getPostalCode())
                .addValue("xhomephone", person.getPrimaryPhone())
                .addValue("xworkphone", person.getSecondaryPhone())
                .addValue("xpassword", user.getPassword())
                .addValue("xtimezone_id", user.getTimezoneId())
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
                .addValue("xcurrency_id", currency)

                //Insert lots of nulls
                .addValue("xaccount_no", null)
                .addValue("xcompany_id", null)
                .addValue("xcountry", null)
                .addValue("xcustom0", null)
                .addValue("xcustom1", null)
                .addValue("xcustom3", null)
                .addValue("xcustom4", null)
                .addValue("xcustom5", null)
                .addValue("xcustom6", null)
                .addValue("xcustom7", null)
                .addValue("xcustom8", null)
                .addValue("xcustom9", null)
                .addValue("xdate_of_birth", null)
                .addValue("xdes_jbtyp_id", null)
                .addValue("xethnicity", null)
                .addValue("xfax", null)
                .addValue("xjob_title", null)
                .addValue("xjobtype_id", null)
                .addValue("xlocale_id", null)
                .addValue("xlocation_id", null)
                .addValue("xmanager_id", null)
                .addValue("xpassword_changed", null)
                .addValue("xperson_no", null)
                .addValue("xperson_type", null)
                .addValue("xreligion", null)
                .addValue("xsecret_answer", null)
                .addValue("xsecret_question", null)
                .addValue("xss_no", null)
                .addValue("xstarted_on", null)
                .addValue("xstatus", null)
                .addValue("xsuffix", null)
                .addValue("xterminated_on", null)
                .addValue("xtitle", null)
                .addValue("xts", null);

        logger.debug("Inserting new user. Executing stored procedure", insertUserStoredProceudreName);

        Map<String, Object> insertUserResults = executeUserStoredProcedure(in, userInsertActor);

        Map<String, Object> insertProfileResults = insertUserProfile(createdByName, createdById, personId,
                split); //TODO this should be wrapped in a transaction, if the second query fails roll back the first

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
                .addValue("xsplit", split)

                //Insert lots of nulls
                .addValue("xcustom0", null)
                .addValue("xcustom1", null)
                .addValue("xcustom3", null)
                .addValue("xcustom4", null)
                .addValue("xcustom5", null)
                .addValue("xcustom6", null)
                .addValue("xcustom7", null)
                .addValue("xcustom8", null)
                .addValue("xcustom9", null)
                .addValue("xcustom0", null)
                .addValue("xorganization_id", null)
                .addValue("xlocation_id", null)
                .addValue("xperson_id", null)
                .addValue("xsyslov1_id", null)
                .addValue("xsyslov2_id", null)
                .addValue("xsyslov3_id", null)
                .addValue("xsyslov4_id", null)
                .addValue("xsyslov5_id", null)
                .addValue("xuserlov1_id", null)
                .addValue("xuserlov2_id", null)
                .addValue("xuserlov3_id", null)
                .addValue("xuserlov4_id", null)
                .addValue("xuserlov5_id", null)
                .addValue("xlongtext0_1", null)
                .addValue("xlongtext0_2", null)
                .addValue("xlongtext0_3", null)
                .addValue("xlongtext0_4", null)
                .addValue("xlongtext0_5", null)
                .addValue("xlongtext0_6", null)
                .addValue("xlongtext0_7", null)
                .addValue("xlongtext0_8", null)
                .addValue("xlongtext1_1", null)
                .addValue("xlongtext1_2", null)
                .addValue("xlongtext1_3", null)
                .addValue("xlongtext1_4", null)
                .addValue("xlongtext1_5", null)
                .addValue("xlongtext1_6", null)
                .addValue("xlongtext1_7", null)
                .addValue("xlongtext1_8", null)
                .addValue("xtext1", null)
                .addValue("xtext2", null)
                .addValue("xtext3", null)
                .addValue("xtext4", null)
                .addValue("xtext5", null)
                .addValue("xdate1", null)
                .addValue("xdate2", null)
                .addValue("xdate3", null)
                .addValue("xint1", null)
                .addValue("xint2", null)
                .addValue("xint3", null)
                .addValue("xnewts", null);

        logger.debug("Inserting user profile. Executing stored procedure: {}", insertProfileStoredProceudreName);
        return executeUserStoredProcedure(in, profileInsertActor);
    }

    private Map<String,Object> executeUserStoredProcedure(SqlParameterSource inParameters, SimpleJdbcCall spCallToExecute) throws Exception {
        try {
            Map<String,Object> out = spCallToExecute.execute(inParameters);

            logger.debug("Stored Procedure executed successfully");
            return out;
        }
        catch (Exception e) {
            logger.error("Error calling stored procedure", e);
            throw e;
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
}
