package net.bridgesapis.bungeebridge.utils;

import java.util.Calendar;
import java.util.Date;

public class Misc {
    public static String formatTime(long time) {
        int days = (int) time / (3600*24);
        int remainder = (int) time - days * (3600*24);
        int hours = remainder / 3600;
        remainder = remainder - (hours * 3600);
        int mins = remainder / 60;

        String ret = "";
        if (days > 0) {
            ret+= days+" jours ";
        }

        if (hours > 0) {
            ret += hours+" heures ";
        }

        if (mins > 0) {
            ret += mins+" minutes ";
        }

        if (ret.equals("") && mins == 0)
            ret += "moins d'une minute";

        return ret;
    }

    public static Date parseTime(String str) {
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        for (String t : str.split("\\+")) {
            String[] end = t.split(":");
            int type;
            if (end[1].equalsIgnoreCase("d"))
                type = Calendar.DAY_OF_YEAR;
            else if (end[1].equalsIgnoreCase("h"))
                type = Calendar.HOUR;
            else if (end[1].equalsIgnoreCase("m"))
                type = Calendar.MINUTE;
            else
                type = Calendar.SECOND;
            cal.add(type, Integer.parseInt(end[0]));
        }
        return cal.getTime();
    }
}
