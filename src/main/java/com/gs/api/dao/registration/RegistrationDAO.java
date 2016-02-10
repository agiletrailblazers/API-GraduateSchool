package com.gs.api.dao.registration;

import com.gs.api.domain.course.CourseSession;

import com.gs.api.domain.registration.User;
import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Date;
import java.util.Map;

@Repository
public class RegistrationDAO {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationDAO.class);
    private JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall insertOfferingActionProfileActor;
    private SimpleJdbcCall insertRegistrationActor;
    private SimpleJdbcCall insertOrderActor;
    private SimpleJdbcCall insertOrderItemActor;
    private SimpleJdbcCall insertChargeActor;
    private SimpleJdbcCall insertPaymentActor;
    private SimpleJdbcCall orderCompleteActor;

    private static final String insertOfferingActionProcedureName = "tpp_offer_action_profile_ins";
    private static final String insertRegistrationProcedureName = "tpp_registration_ins";
    private static final String insertOrderProcedureName = "tpp_oe_order_ins";
    private static final String insertOrderItemProcedureName = "tpp_oe_item_reg_ins";
    private static final String insertChargeProcedureName = "tpp_charge_ins";
    private static final String insertPaymentProcedureName = "lep_payment_info_ins";
    private static final String orderCompleteProcedureName = "tpp_oe_tran_complete";

    private static final String getOfferingActionSequenceQuery = "select lpad(ltrim(rtrim(to_char(tpt_offer_action_profile_seq.nextval))), 15, '0') id from dual";
    private static final String getRegistrationSequenceQuery = "select lpad(ltrim(rtrim(to_char(tpt_regist_seq.nextval))), 15, '0') id from dual";
    private static final String getOrderSequenceQuery = "select lpad(ltrim(rtrim(to_char(tpt_oe_order_seq.nextval))), 15, '0') id from dual";
    private static final String getOrderItemSequenceQuery = "select lpad(ltrim(rtrim(to_char(tpt_oe_item_reg_seq.nextval))), 15, '0') id from dual";
    private static final String getChargeSequenceQuery = "select lpad(ltrim(rtrim(to_char(tpt_charge_seq.nextval))), 15, '0') id from dual";
    private static final String getPaymentSequenceQuery = "select lpad(ltrim(rtrim(to_char(let_payment_info_seq.nextval))), 15, '0') id from dual";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.insertOfferingActionProfileActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertOfferingActionProcedureName);
        this.insertRegistrationActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertRegistrationProcedureName);
        this.insertOrderActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertOrderProcedureName);
        this.insertOrderItemActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertOrderItemProcedureName);
        this.insertChargeActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertChargeProcedureName);
        this.insertPaymentActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(insertPaymentProcedureName);
        this.orderCompleteActor = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName(orderCompleteProcedureName);
    }
    /**
     * Create a registration for the requested session.
     * @param user is the user that is performing the registration.  This may or may not be the ID of the student being registered.
     * @param student is the user account of the student who is registering for the course.
     * @param session is the session which is being signed up for
     * @return the id of the created registration.
     * @throws Exception error creating registration.
     */
    public String register(User user, User student, CourseSession session) throws Exception {
        logger.debug("Inserting registration into the database");

        String offeringActionProfileId = insertOfferingActionProfile(user, student, session);
        String registrationId = insertRegistration(student, session, offeringActionProfileId);
        String orderId = insertOrder(user, student, session);
        String orderItemId = insertOrderItem(user, student, session, registrationId, orderId);
        String chargeId = insertCharge(user, session, orderItemId);
        String paymentId = insertPaymentInfo(user, student, session, orderId);

        /* The stored procedure "tpp_cancel_recurring" is executed around this point which sets the registration status
        to 600 (canceled), cancels the order, and frees up the seat. This doesn't make sense, and the resulting registration in the
         DB has a status of 100, which means procedure never actually changed it. As such, we are not currently calling that SP */

        completeOrder(orderId);

        return registrationId;
    }

    private String insertOfferingActionProfile(User user, User student, CourseSession session) throws Exception {
        String offeringActionProfileId = "ofapr" + this.jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class);

        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", offeringActionProfileId, OracleTypes.FIXED_CHAR)
                .addValue("xcreated_on", currentDate, OracleTypes.DATE)
                .addValue("xupdated_on", currentDate, OracleTypes.DATE)
                .addValue("xparty_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                .addValue("xadded_to_profile_on", currentDate, OracleTypes.DATE)
                .addValue("xoffering_temp_id", session.getCourseId(), OracleTypes.FIXED_CHAR)
                .addValue("xstart_date", session.getStartDate(), OracleTypes.DATE) //Do we need to convert date?
                .addValue("xoffrng_start_date", session.getStartDate(), OracleTypes.DATE) //Convert date?
                .addValue("xcreated_by", user.getUsername(), OracleTypes.VARCHAR)
                .addValue("xcreated_id", user.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xupdated_by", user.getUsername(), OracleTypes.VARCHAR)
                //hardcoded values
                .addValue("xstatus", 100, OracleTypes.INTEGER)
                .addValue("xflags", "2", OracleTypes.FIXED_CHAR)
                .addValue("xscore", 0, OracleTypes.FLOAT)
                .addValue("xaction_status", 100, OracleTypes.INTEGER)
                //nulls
                .addValue("xtime_stamp", null, OracleTypes.VARCHAR)
                .addValue("xaction_no", null, OracleTypes.VARCHAR)
                .addValue("xgrade", null, OracleTypes.VARCHAR)
                .addValue("xcompletion_date", null, OracleTypes.DATE)
                .addValue("xtarget_date", null, OracleTypes.DATE)
                .addValue("xdelivered_by", null, OracleTypes.FIXED_CHAR)
                .addValue("xlocation", null, OracleTypes.VARCHAR)
                .addValue("xdel_type", null, OracleTypes.VARCHAR)
                .addValue("xstart_time", null, OracleTypes.VARCHAR)
                .addValue("xend_time", null, OracleTypes.VARCHAR)
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
                .addValue("xend_date", null, OracleTypes.DATE)
                .addValue("xduration", null, OracleTypes.FLOAT);

        logger.debug("Inserting OfferingActionProfile. Executing stored procedure: {}", insertOfferingActionProcedureName);
        Map<String, Object> insertOfferingActionResults = executeRegistrationStoredProcedure(in, insertOfferingActionProfileActor);

        return offeringActionProfileId;
    }

    private String insertRegistration(User student, CourseSession session, String offeringActionProfileId) throws Exception {
        String registrationId = "regdw" + this.jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class);
        long millis = new Date().getTime();

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", registrationId, OracleTypes.FIXED_CHAR)
                .addValue("xclass_id", session.getOfferingSessionId(), OracleTypes.FIXED_CHAR)
                .addValue("xstudent_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                .addValue("xoffer_action_id", offeringActionProfileId, OracleTypes.FIXED_CHAR)
                //hardcode till we figure out something better
                .addValue("xstatus", 100, OracleTypes.INTEGER)
                .addValue("xwlist_priority", 5, OracleTypes.INTEGER)
                .addValue("xreg_type", 200, OracleTypes.INTEGER)
                .addValue("xflags", "0000000000", OracleTypes.FIXED_CHAR)
                //nulls
                .addValue("xwlist_on", null, OracleTypes.DATE)
                .addValue("xtime_stamp", null, OracleTypes.VARCHAR)
                .addValue("xreg_no", null, OracleTypes.VARCHAR)
                .addValue("xmax_count", null, OracleTypes.INTEGER)
                .addValue("xcurrent_count", null, OracleTypes.INTEGER)
                .addValue("xfrom_date", null, OracleTypes.DATE)
                .addValue("xto_date", null, OracleTypes.DATE)
                .addValue("xros_temp_en_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xcustom0", null, OracleTypes.VARCHAR)
                .addValue("xcustom1", null, OracleTypes.VARCHAR)
                .addValue("xcustom2", null, OracleTypes.VARCHAR)
                .addValue("xcustom3", null, OracleTypes.VARCHAR)
                .addValue("xcustom4", null, OracleTypes.VARCHAR)
                .addValue("xcustom5", null, OracleTypes.VARCHAR)
                .addValue("xcustom6", null, OracleTypes.VARCHAR)
                .addValue("xcustom7", null, OracleTypes.VARCHAR)
                .addValue("xcustom8", null, OracleTypes.VARCHAR)
                .addValue("xcustom9", null, OracleTypes.VARCHAR);

        logger.debug("Inserting Registration. Executing stored procedure: {}", insertRegistrationProcedureName);
        Map<String, Object> insertRegistrationResults = executeRegistrationStoredProcedure(in, insertRegistrationActor);

        return registrationId;
    }

    private String insertOrder(User user, User student, CourseSession session) throws Exception {
        //Note: When refactoring for training officials, this will have to find the existing order's id and not insert a new one
        String orderId = "intor" + this.jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class);
        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        //These could be retrieved from the user
        String split = "domin000000000000001";
        String currencyID = "crncy000000000000167";

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", orderId, OracleTypes.FIXED_CHAR)
                .addValue("xcreated_id", user.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xcreated_on", currentDate, OracleTypes.DATE)
                .addValue("xcreated_by", user.getUsername(), OracleTypes.VARCHAR)
                .addValue("xupdated_on", currentDate, OracleTypes.DATE)
                .addValue("xupdated_by", user.getUsername(), OracleTypes.VARCHAR)
                .addValue("xsold_by_id", user.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xtotal_charges", session.getTuition(), OracleTypes.FLOAT)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                .addValue("xcompany_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xdept_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xcontact_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xaccount_id", student.getAccountId(), OracleTypes.FIXED_CHAR)
                .addValue("xsplit", split, OracleTypes.VARCHAR)
                .addValue("xcurrency_id", currencyID, OracleTypes.FIXED_CHAR)
                //Hardcoded till we get something better
                .addValue("xstatus", 100, OracleTypes.INTEGER)
                .addValue("xstatus_flag", "000010000", OracleTypes.FIXED_CHAR)
                .addValue("xconf_type", 0, OracleTypes.INTEGER)
                .addValue("xproxy", "0", OracleTypes.FIXED_CHAR)
                //nulls
                .addValue("xtime_stamp", null, OracleTypes.VARCHAR)
                .addValue("xorder_no", null, OracleTypes.VARCHAR)
                .addValue("xdiscount", null, OracleTypes.FLOAT)
                .addValue("xclient_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xpo_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xcustom0", null, OracleTypes.VARCHAR)
                .addValue("xcustom1", null, OracleTypes.VARCHAR)
                .addValue("xcustom2", null, OracleTypes.VARCHAR)
                .addValue("xcustom3", null, OracleTypes.VARCHAR)
                .addValue("xcustom4", null, OracleTypes.VARCHAR)
                .addValue("xshipped_attn", null, OracleTypes.VARCHAR)
                .addValue("xaddr1", null, OracleTypes.VARCHAR)
                .addValue("xaddr2", null, OracleTypes.VARCHAR)
                .addValue("xcity", null, OracleTypes.VARCHAR)
                .addValue("xstate", null, OracleTypes.VARCHAR)
                .addValue("xzip", null, OracleTypes.VARCHAR)
                .addValue("xcountry", null, OracleTypes.VARCHAR)
                .addValue("xsource_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xterritory_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xcr_card_no", null, OracleTypes.VARCHAR)
                .addValue("xcr_card_type", null, OracleTypes.VARCHAR)
                .addValue("xname", null, OracleTypes.VARCHAR)
                .addValue("xexp_date", null, OracleTypes.DATE)
                .addValue("xau_code", null, OracleTypes.VARCHAR)
                .addValue("xcustom5", null, OracleTypes.VARCHAR)
                .addValue("xcustom6", null, OracleTypes.VARCHAR)
                .addValue("xcustom7", null, OracleTypes.VARCHAR)
                .addValue("xcustom8", null, OracleTypes.VARCHAR)
                .addValue("xcustom9", null, OracleTypes.VARCHAR)
                .addValue("xauth_info", null, OracleTypes.VARCHAR)
                .addValue("xcustom10", null, OracleTypes.VARCHAR)
                .addValue("xcustom11", null, OracleTypes.VARCHAR)
                .addValue("xcustom12", null, OracleTypes.VARCHAR)
                .addValue("xcustom13", null, OracleTypes.VARCHAR)
                .addValue("xcustom14", null, OracleTypes.VARCHAR);

        logger.debug("Inserting Order. Executing stored procedure: {}", insertOrderProcedureName);
        Map<String, Object> insertOrderResults = executeRegistrationStoredProcedure(in, insertOrderActor);
        return orderId;
    }

    private String insertOrderItem(User user, User student, CourseSession session, String registrationId, String orderId) throws Exception {
        String orderItemId = "ioreg" + this.jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class);

        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", orderItemId, OracleTypes.FIXED_CHAR)
                .addValue("xorder_id", orderId, OracleTypes.FIXED_CHAR)
                .addValue("xtotal_cost", session.getTuition(), OracleTypes.FLOAT)
                .addValue("xreg_id", registrationId, OracleTypes.FIXED_CHAR)
                .addValue("xpart_id", session.getOfferingSessionId(), OracleTypes.FIXED_CHAR)
                .addValue("xstudent_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xcreated_on", currentDate, OracleTypes.DATE)
                .addValue("xcreated_by", user.getUsername(), OracleTypes.VARCHAR)
                .addValue("xno_units", session.getTuition(), OracleTypes.INTEGER)
                .addValue("xunit_cost", session.getTuition(), OracleTypes.FLOAT)
                .addValue("xoffering_template_id", session.getCourseId(), OracleTypes.FIXED_CHAR)
                .addValue("xcreated_id", user.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                //hardcode till we know better
                .addValue("xstatus", 100, OracleTypes.INTEGER)
                .addValue("xreq_qty", 1, OracleTypes.INTEGER) //Number of seats?
                .addValue("xseq_no", 1, OracleTypes.INTEGER)
                .addValue("xflags", "0000", OracleTypes.FIXED_CHAR)
                .addValue("xapproved", "1", OracleTypes.VARCHAR)
                .addValue("xapproved_status", 400, OracleTypes.INTEGER)
                .addValue("xact_qty", 0, OracleTypes.INTEGER)
                .addValue("xbilling_status", 200, OracleTypes.INTEGER)
                //nulls
                .addValue("xtime_stamp", null, OracleTypes.VARCHAR)
                .addValue("xpkg_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xdescription", null, OracleTypes.VARCHAR)
                .addValue("xpkg_item_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xclass_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xnotes", null, OracleTypes.VARCHAR)
                .addValue("xitem_no", null, OracleTypes.VARCHAR)
                .addValue("xtunit_discount_id", null, OracleTypes.FIXED_CHAR)
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
                .addValue("xdelivered_on", null, OracleTypes.DATE)
                .addValue("xapproved_on", null, OracleTypes.DATE)
                .addValue("xapproved_by", null, OracleTypes.VARCHAR)
                .addValue("xbill_no", null, OracleTypes.VARCHAR)
                .addValue("xbilled_on", null, OracleTypes.DATE);

        logger.debug("Inserting Order Item. Executing stored procedure: {}", insertOrderItemProcedureName);
        Map<String, Object> insertOrderItemResults = executeRegistrationStoredProcedure(in, insertOrderItemActor);

        return orderItemId;
    }

    private String insertCharge(User user, CourseSession session, String orderItemId) throws Exception {
        String chargeId = "chrgs" + this.jdbcTemplate.queryForObject(getChargeSequenceQuery, String.class);

        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        java.sql.Date payDate = new java.sql.Date(millis);

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", chargeId, OracleTypes.FIXED_CHAR)
                .addValue("xamount", session.getTuition(), OracleTypes.FLOAT)
                .addValue("xowner_id", orderItemId, OracleTypes.FIXED_CHAR)
                .addValue("xcreated_by", user.getUsername(), OracleTypes.VARCHAR)
                //Hardcode till we know better
                .addValue("xpaycat_id", "4000", OracleTypes.FIXED_CHAR)
                .addValue("xpay_date", payDate, OracleTypes.DATE)
                //Nulls
                .addValue("xref_id", null, OracleTypes.FIXED_CHAR)
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
                .addValue("xcustom10", null, OracleTypes.VARCHAR)
                .addValue("xnote", null, OracleTypes.VARCHAR)
                .addValue("xspot_rate", null, OracleTypes.FLOAT)
                .addValue("xspot_ccy_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xnewts", null, OracleTypes.VARCHAR);

        logger.debug("Inserting charge. Executing stored procedure: {}", insertChargeProcedureName);
        Map<String, Object> insertChargeResults = executeRegistrationStoredProcedure(in, insertChargeActor);

        return chargeId;
    }

    private String insertPaymentInfo(User user, User student, CourseSession session, String orderId) throws Exception {
        String paymentId = "mopay" + this.jdbcTemplate.queryForObject(getPaymentSequenceQuery, String.class);

        //Setup audit data
        Date currentDate = new Date();
        long millis = currentDate.getTime();

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xid", paymentId, OracleTypes.FIXED_CHAR)
                .addValue("xcreated_on", currentDate, OracleTypes.DATE)
                .addValue("xcreated_by", user.getUsername(), OracleTypes.VARCHAR)
                .addValue("xupdated_on", currentDate, OracleTypes.DATE)
                .addValue("xupdated_by", user.getUsername(), OracleTypes.VARCHAR)
                .addValue("xowner_id", orderId, OracleTypes.FIXED_CHAR)
                .addValue("xcontact_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xcompany_id", student.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xcreated_id", user.getId(), OracleTypes.FIXED_CHAR)
                .addValue("xmoney_amt", session.getTuition(), OracleTypes.FLOAT)
                .addValue("xnewts", millis, OracleTypes.VARCHAR)
                //Hardcode till we know better
                .addValue("xcheck_no", "000000000000", OracleTypes.VARCHAR)
                .addValue("xsplit", "domin000000000000001", OracleTypes.VARCHAR) // Can likely get from user
                .addValue("xcurrency_id", "crncy000000000000167", OracleTypes.FIXED_CHAR) //Can likely get from user
                .addValue("xconf_type", "3", OracleTypes.VARCHAR)
                .addValue("xstatus", "1", OracleTypes.VARCHAR)
                .addValue("xtx_status", "0", OracleTypes.VARCHAR)
                //Nulls
                .addValue("xtime_stamp", null, OracleTypes.VARCHAR)
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
                .addValue("xcr_card_no", null, OracleTypes.VARCHAR)
                .addValue("xcr_card_type", null, OracleTypes.VARCHAR)
                .addValue("xcr_holder_name", null, OracleTypes.VARCHAR)
                .addValue("xcr_email", null, OracleTypes.VARCHAR)
                .addValue("xexp_date", null, OracleTypes.DATE)
                .addValue("xpo_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xau_code", null, OracleTypes.VARCHAR)
                .addValue("xau_info", null, OracleTypes.VARCHAR)
                .addValue("xcc_no_masked", null, OracleTypes.VARCHAR)
                .addValue("xcc_sec_code", null, OracleTypes.VARCHAR)
                .addValue("xcc_saba_tx_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xcc_gateway_tx_id", null, OracleTypes.VARCHAR)
                .addValue("xcc_tx_result_code", null, OracleTypes.VARCHAR)
                .addValue("xbank_draft_no", null, OracleTypes.VARCHAR)
                .addValue("xwr_tfr_tx_no", null, OracleTypes.VARCHAR)
                .addValue("xref_money_amt", null, OracleTypes.FLOAT)
                .addValue("xtu_qty", null, OracleTypes.INTEGER)
                .addValue("xref_tu_qty", null, OracleTypes.INTEGER)
                .addValue("xtu_agr_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xparent_id", null, OracleTypes.FIXED_CHAR)
                .addValue("xaddr1", null, OracleTypes.VARCHAR)
                .addValue("xaddr2", null, OracleTypes.VARCHAR)
                .addValue("xaddr3", null, OracleTypes.VARCHAR)
                .addValue("xcity", null, OracleTypes.VARCHAR)
                .addValue("xstate", null, OracleTypes.VARCHAR)
                .addValue("xzip", null, OracleTypes.VARCHAR)
                .addValue("xcountry", null, OracleTypes.VARCHAR);

        logger.debug("Inserting payment info. Executing stored procedure: {}", insertPaymentProcedureName);
        Map<String, Object> insertPaymentResults = executeRegistrationStoredProcedure(in, insertPaymentActor);

        return paymentId;
    }

    private void completeOrder(String orderId) throws Exception {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("xorder_id", orderId, OracleTypes.FIXED_CHAR);

        logger.debug("Completing order. Executing stored procedure: {}", orderCompleteProcedureName);
        Map<String, Object> completeOrderResults = executeRegistrationStoredProcedure(in, orderCompleteActor);
    }

    private Map<String,Object> executeRegistrationStoredProcedure(MapSqlParameterSource inParameters, SimpleJdbcCall spCallToExecute) throws Exception {
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
}
