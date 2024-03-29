package com.muva.bamburi.utils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Njoro on 3/27/18.
 */

public class TimeAgo {

    public static final Map<String, Long> times = new LinkedHashMap<>();

    static {
        times.put("y", TimeUnit.DAYS.toMillis(365));
        times.put("m", TimeUnit.DAYS.toMillis(30));
        times.put("wk", TimeUnit.DAYS.toMillis(7));
        times.put("d", TimeUnit.DAYS.toMillis(1));
        times.put("h", TimeUnit.HOURS.toMillis(1));
        times.put("min", TimeUnit.MINUTES.toMillis(1));
        times.put("s", TimeUnit.SECONDS.toMillis(1));
    }

    public static String toRelative(long duration, int maxLevel) {
        StringBuilder res = new StringBuilder();
        int level = 0;
        for (Map.Entry<String, Long> time : times.entrySet()) {
            long timeDelta = duration / time.getValue();
            if (timeDelta > 0) {
                res.append(timeDelta)
                        .append("")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "" : "")
                        .append(", ");
                duration -= time.getValue() * timeDelta;
                level++;
            }
            if (level == maxLevel) {
                break;
            }
        }
        if ("".equals(res.toString())) {
            return "Just Now";
        } else {
            res.setLength(res.length() - 2);
//            res.append(" ago");
            return res.toString();
        }
    }

    public static String toRelative(long duration) {
        return toRelative(duration, times.size());
    }

    public static String toRelative(Date start, Date end) {
        assert start.after(end);
        return toRelative(end.getTime() - start.getTime());
    }

    public static String toRelative(Date start, Date end, int level) {
        assert start.after(end);
        return toRelative(end.getTime() - start.getTime(), level);
    }
}
