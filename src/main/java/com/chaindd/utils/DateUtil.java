package com.chaindd.utils;


import com.chaindd.Constants.DataConstants;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Author: xinyueyan
 * @Date: 1/6/2019 12:09 PM
 */
public class DateUtil {
    //获取当前格林威治时间
    public static String getNowTime(String format){
        //format= DataConstants.DATE_FORMAT_1;
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(gmtZone);
        Date timeNow = cd.getTime();
        String dateNowStr = sdf.format(timeNow);
        return dateNowStr;


    }
    //字符串转化成可比较时间戳
    public static long comparableTimestamp(String dateStr,String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        long timestamp = sdf.parse(dateStr).getTime() / 1000;
        return timestamp;

    }

    //比较两个时间戳
    public static int compareDate(String format,String newDate,String dateToCompare) throws ParseException {
        //获取当前格林威治时间
        long now_timestamp = comparableTimestamp(newDate,format);
        long date_timestamp = comparableTimestamp(dateToCompare, format);
        if (now_timestamp<date_timestamp)
            return 1;
        else if(now_timestamp>date_timestamp)
            return 3;
        else
            return 2;

    }

    /**
     * long戳转date字符串
     * @param
     * @return
     */
    public static String formatTransform(String dateStr,String fromFormat,String toFormat) throws ParseException {
        //处理st  nd  rd  th
        String[] split = dateStr.split(" ");
        if(split[0].contains("st")||split[0].contains("nd")||split[0].contains("rd")||split[0].contains("th"))
            split[0] = split[0].substring(0,split[0].length()-2);
        dateStr=split[0]+" "+split[1]+" "+split[2];
        String res;
        long l = comparableTimestamp(dateStr, fromFormat);//秒
        Date date = new Date(l*1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(toFormat);
        res = simpleDateFormat.format(date);
        return res;
    }

    /*public static void main(String[] args) throws ParseException {
        String date = "1st Dec 2017";
        String s = formatTransform(date, DataConstants.DATE_FORMAT_1, DataConstants.DATE_FORMAT_2);
        System.out.println("==="+s);
    }*/
}
