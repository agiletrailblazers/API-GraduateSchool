package com.gs.api.search.util;

import com.gs.api.domain.course.CourseSessionDomain;
import com.gs.api.domain.course.CourseSessionStatus;
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
        courseSessionStatus.add(CourseSessionStatus.C.name());
        courseSessionStatus.add(CourseSessionStatus.S.name());
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
            courseSessionStatus.add(CourseSessionStatus.C.name());
            courseSessionStatus.add(CourseSessionStatus.S.name());
        }
        if (StringUtils.isNotEmpty(sessionDomain)) {
            if (sessionDomain.equalsIgnoreCase(CourseSessionDomain.CD.name())) {
                courseSessionDomain.add(CourseSessionDomain.domin000000000001085.name());
            } else if (sessionDomain.equalsIgnoreCase(CourseSessionDomain.EP.name())) {
                courseSessionDomain.add(CourseSessionDomain.domin000000000001089.name());
            } else {
                courseSessionDomain.add(sessionDomain);
            }
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("courseSessionStatus", courseSessionStatus);
        params.put("courseSessionDomain", courseSessionDomain);

        return params;

    }
}
