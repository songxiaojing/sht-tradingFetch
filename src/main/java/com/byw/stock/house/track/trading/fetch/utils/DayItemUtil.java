package com.byw.stock.house.track.trading.fetch.utils;

import java.text.SimpleDateFormat;


public class DayItemUtil {

    final public static SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    final public static long DAY_INTERVAL = 1000 * 60 * 60 * 24;

    //    public static String[] buildFetchItemsByDay(long startPoint, int days) {
    //
    //        if (days < 0) {
    //            return null;
    //        }
    //        String[] fetchItems = new String[days];
    //        for (int i = 0; i < days; i++) {
    //            fetchItems[i] = DEFAULT_DATE_FORMATTER.format(new Date(startPoint - i * DAY_INTERVAL));
    //        }
    //        return fetchItems;
    //    }
    //
    //    public static void main(String args[]) {
    //
    //        Date today = new Date();
    //        String[] items = new DayItemUtil().buildFetchItemsByDay(today.getTime(), 365);
    //        for (String show : items) {
    //            System.out.println(show);
    //        }
    //    }
}
