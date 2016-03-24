package com.gs.api.search.util;

import com.gs.api.domain.course.CourseSessionDomain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionQueryParamsBuilder {

    /**
     * build query parameter map to get the session for given session id
     * @param sessionId the sessionId
     * @return params Map
     */
    public Map<String,Object> buildSessionQueryParams(String sessionId) {
        List<String> courseSessionStatus = new ArrayList<String>();
        List<String> courseSessionId = new ArrayList<String>();
        courseSessionStatus.add(CourseSessionDomain.C.name());
        courseSessionStatus.add(CourseSessionDomain.S.name());
        courseSessionId.add(sessionId);
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("courseSessionStatus",courseSessionStatus);
        params.put("courseSessionId",courseSessionId);
        return  params;

    }

    /**
     * build query parameter map to get the session for given session id
     * @param status the session status C-G2G
     * @param sessionDomain the session domain Type
     * @return params Map
     */
    public Map<String,Object> buildCourseSessionsQueryParams(String status,String sessionDomain) {
        List<String> courseSessionStatus = new ArrayList<String>();
        List<String> courseSessionDomain = new ArrayList<String>();
        if (StringUtils.isNotEmpty(status)) {
            courseSessionStatus.add(status.toUpperCase());
        } else {
            courseSessionStatus.add(CourseSessionDomain.C.name());
            courseSessionStatus.add(CourseSessionDomain.S.name());
        }
        if (StringUtils.isNotEmpty(sessionDomain)) {
            if (sessionDomain.equalsIgnoreCase(CourseSessionDomain.CD.name())) {
                courseSessionDomain.add(CourseSessionDomain.domin000000000001085.name());
            } else if (sessionDomain.equalsIgnoreCase(CourseSessionDomain.EP.name())) {
                courseSessionDomain.add(CourseSessionDomain.domin000000000001089.name());
            } else {
                courseSessionDomain.add(sessionDomain);
            }
        } else {
            courseSessionDomain.add(CourseSessionDomain.domin000000000001081.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001082.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001083.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001084.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001085.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001086.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001087.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001088.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001089.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001090.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001091.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001092.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001095.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000000001.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000002040.name());
            courseSessionDomain.add(CourseSessionDomain.domin000000000001420.name());
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("courseSessionStatus", courseSessionStatus);
        params.put("courseSessionDomain", courseSessionDomain);

        return params;

    }
}
