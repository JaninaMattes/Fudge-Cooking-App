package com.foodtracker.foodtrackerapi.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class to convert the incoming date string into the correct date
 * format before it is processed.
 * 
 * @author Janina Mattes
 */

public class DateUtils {

    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public static String convertDateFormat(String dateStr){
        // formats a date string into the correct format
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
      
        Date inputDate  = new Date();
        try {
            inputDate = inputFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String outputDateStr = outputFormat.format(inputDate);
        return outputDateStr;
    }
}
