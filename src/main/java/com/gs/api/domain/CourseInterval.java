package com.gs.api.domain;

import java.util.HashMap;

public enum CourseInterval {
    HOUR("Hr", 60), 
    DAY("Day", 1440), 
    WEEK("Wk", 10080),
    MONTH("Mth", 43200), 
    YEAR("Yr", 525600),
    VARIABLE("Variable", 1);
    
    private String interval;
    private int durationDenominator;
    
    /**
     * Internal mapping form String to Enum value
     */
    @SuppressWarnings("serial")
    static final HashMap<String, CourseInterval> VALUES = new HashMap<String , CourseInterval>() {{
        put("Hr",  CourseInterval.HOUR);
        put("Day", CourseInterval.DAY);
        put("Wk",  CourseInterval.WEEK);
        put("Mth",  CourseInterval.MONTH);
        put("Yr",  CourseInterval.YEAR);
        put("Variable",  CourseInterval.VARIABLE);
    }};
        
    /**
     * Private constructor
     * @param interval
     * @param durationDenominator
     */
    private CourseInterval(String interval, int durationDenominator) {
        this.interval = interval;
        this.durationDenominator = durationDenominator;
    }
    
    /**
     * Get the Enum value from the String
     * @param interval
     * @return
     */
    public static CourseInterval valueFor(String interval) {
        return VALUES.get(interval);
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public int getDurationDenominator() {
        return durationDenominator;
    }

    public void setDurationDenominator(int durationDenominator) {
        this.durationDenominator = durationDenominator;
    }
    
    /**
     * Perform a calculation to get the duration given the interval
     * @param duration
     * @return Duration in units of interval
     */
    public int getDuration(int duration) {
        return duration / durationDenominator;
    }
    
}
