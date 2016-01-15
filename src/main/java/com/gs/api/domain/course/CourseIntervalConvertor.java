package com.gs.api.domain.course;

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
        intervalMap.put("HR", "Hour");
        intervalMap.put("'MNTH", "Month");
        intervalMap.put("MI", "Minute");
    }

    /**
     * Convert an interval value to a real value
     * @param interval the item to convert
     * @return String
     */
    public static String getInterval(String interval) {
        return intervalMap.get(interval);
    }

}
