package com.gs.api.domain.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gs.api.domain.Person;

@JsonInclude(Include.ALWAYS)
public class UserProfile {

    private String id;
    private String userId;

    /* From Main Insert Query */
    /* xid IN CHAR, xtime_stamp IN VARCHAR2, xcreated_on IN DATE, xupdated_on IN DATE, xcreated_by IN VARCHAR2, xupdated_by IN VARCHAR2,
    xcreated_id IN CHAR, xcustom0 IN VARCHAR2, xcustom1 IN VARCHAR2, xcustom2 IN VARCHAR2, xcustom3 IN VARCHAR2, xcustom4 IN VARCHAR2,
    xcustom5 IN VARCHAR2, xcustom6 IN VARCHAR2, xcustom7 IN VARCHAR2, xcustom8 IN VARCHAR2, xcustom9 IN VARCHAR2, xsplit IN CHAR,
    xflags IN CHAR, xprofiled_id IN VARCHAR2, xentry_type_id IN VARCHAR2, xjob_type_id IN CHAR, xorganization_id IN CHAR,
    xlocation_id IN CHAR, xperson_id IN CHAR, xsyslov1_id IN VARCHAR2, xsyslov2_id IN VARCHAR2, xsyslov3_id IN VARCHAR2,
    xsyslov4_id IN VARCHAR2, xsyslov5_id IN VARCHAR2, xuserlov1_id IN VARCHAR2, xuserlov2_id IN VARCHAR2, xuserlov3_id IN VARCHAR2,
    xuserlov4_id IN VARCHAR2, xuserlov5_id IN VARCHAR2, xlongtext0_1 IN VARCHAR2, xlongtext0_2 IN VARCHAR2, xlongtext0_3 IN VARCHAR2,
    xlongtext0_4 IN VARCHAR2, xlongtext0_5 IN VARCHAR2, xlongtext0_6 IN VARCHAR2, xlongtext0_7 IN VARCHAR2, xlongtext0_8 IN VARCHAR2,
    xlongtext1_1 IN VARCHAR2, xlongtext1_2 IN VARCHAR2, xlongtext1_3 IN VARCHAR2, xlongtext1_4 IN VARCHAR2, xlongtext1_5 IN VARCHAR2,
    xlongtext1_6 IN VARCHAR2, xlongtext1_7 IN VARCHAR2, xlongtext1_8 IN VARCHAR2, xtext1 IN VARCHAR2, xtext2 IN VARCHAR2, xtext3 IN VARCHAR2,
    xtext4 IN VARCHAR2, xtext5 IN VARCHAR2, xdate1 IN DATE, xdate2 IN DATE, xdate3 IN DATE, xint1   IN  INT, xint2   IN  INT, xint3   IN  INT,
    xnewts IN VARCHAR2, xlocale_id IN CHAR */

    public UserProfile() {}

    public UserProfile(String id, String username, String password, Person person) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
