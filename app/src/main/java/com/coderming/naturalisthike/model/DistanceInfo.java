package com.coderming.naturalisthike.model;

import com.coderming.naturalisthike.utils.Utility;

/**
 * Created by linna on 9/13/2016.
 */
public class DistanceInfo {
    static final String formatter = "%s and %s from %s to %s";
    static final String formatter2 = "%s, %s";
    static final String debugFormatter = "%s, %s (%s) from %s to %s";
    public String destAddress;
    public String origAddress;
    public String distanceText;
    public String durationText;
    public int distance;                  // meter
    public long duration;                  // millsec

    public String toDebugString() {
        String calcDuration = Utility.formatTimeDuration(duration);
        return String.format(debugFormatter, distanceText, durationText, calcDuration, origAddress, destAddress);
//        return String.format(formatter2, distanceText, durationText);
    }
    @Override
    public String toString() {
        String calcDuration = Utility.formatTimeDuration(duration);
        return String.format(formatter2, distanceText, durationText);
    }
    public String toString(String from, String to) {
        return String.format(formatter, durationText, distanceText, from, to );
    }
}
