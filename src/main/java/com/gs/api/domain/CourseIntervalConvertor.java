package com.gs.api.domain;

import java.util.HashMap;
import java.util.Map;

public class CourseIntervalConvertor {

    private static final Map<String, String> intervalMap;
    
    static {
        intervalMap = new HashMap<String, String>();
        intervalMap.put("DY", "Day");
        intervalMap.put("MN", "Month");
        intervalMap.put("YR", "Year");
        intervalMap.put("WK", "Week");
    }

    /**
     * Convert an interval value to a real value
     * @param interval
     * @return String
     */
    public final static String getInterval(String interval) {
        return intervalMap.get(interval);
    }
    
}
