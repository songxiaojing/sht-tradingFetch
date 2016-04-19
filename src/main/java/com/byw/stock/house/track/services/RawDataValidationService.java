package com.byw.stock.house.track.services;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import com.byw.stock.house.track.utils.Util;


public class RawDataValidationService {

    private HashMap<String, String> _holidayMap = new HashMap<String, String>();
    private Calendar c = Calendar.getInstance();

    public void start() {

        this.loadHolidayDataFromFile();
    }

    public boolean isHoliday(String dateText) {

        return this._holidayMap.containsKey(dateText);
    }

    public boolean isWeekend(Date validationDate) {

        synchronized (c) {
            c.setTime(validationDate);
            int weedDay = c.get(Calendar.DAY_OF_WEEK);
            if (weedDay == Calendar.SUNDAY || weedDay == Calendar.SATURDAY) {
                return true;
            }
        }
        return false;
    }

    private void loadHolidayDataFromFile() {

        try {
            byte[] fileBytes = Util.readInputStream2ByteArray(this.getClass().getClassLoader().getResourceAsStream("holiday.json"));
            String jsonText = new String(fileBytes, "UTF-8");
            JSONArray js = new JSONArray(jsonText);
            for (int i = 0; i < js.length(); i++) {
                _holidayMap.put(js.get(i).toString(), null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        RawDataValidationService rds = new RawDataValidationService();
        rds.start();
        System.out.println("isHoliday:2015-03-01" + rds.isHoliday("2015-01-01"));
        System.out.println(rds._holidayMap);
        Calendar c = Calendar.getInstance();

        System.out.println(c.getFirstDayOfWeek());
    }

}
