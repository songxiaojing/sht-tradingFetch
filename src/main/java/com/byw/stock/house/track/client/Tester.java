package com.byw.stock.house.track.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.byw.stock.house.track.services.MongoDBStorageService;
import com.byw.stock.house.track.utils.Util;


public class Tester {

    final public static MathContext BIGDECIMAL_POINT_4 = new MathContext(4);

    public static void main(String[] args) throws UnsupportedEncodingException {

    }

    public static void priceFormat() {

        float p = 10.130000114440918f;
        BigDecimal bd = new BigDecimal(p, BIGDECIMAL_POINT_4);
        System.out.println(bd.toEngineeringString());
        System.out.println(bd.floatValue());
    }

    public static void findDateFromResponseHeader() {

        String contentDisposition = "attachment; filename=\"sh600900_ﾳ￉ﾽﾻￃ￷ￏﾸ_2011-07-08.xls\"";
        if (contentDisposition != null) {
            String[] fileNames = contentDisposition.split(";");
            for (int i = 0; i < fileNames.length; i++) {
                String data = fileNames[i].trim();
                if (data.startsWith("filename=") == true) {
                    System.out.println(data.substring(data.indexOf("\"") + 1, data.indexOf("_")));
                    System.out.println(data.substring(data.lastIndexOf("_") + 1, data.indexOf(".")));
                    break;
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static void writeStockCodeListIntoMongoFromFile() {

        try {
            MongoDBStorageService _mss = new MongoDBStorageService("192.168.72.36:27017", "STOCK");
            _mss.start();

            byte[] fileBytes = Util.readFileContent2ByteArray(new File("/opt/hadoop/stock/stock_list.json"));
            String jsonText = new String(fileBytes, "UTF-8");
            JSONObject js = new JSONObject(jsonText);
            List<Document> updateList = new ArrayList<Document>();
            for (Iterator keyIter = js.keys(); keyIter.hasNext();) {
                String keyName = (String) keyIter.next();
                Document doc = new Document();
                doc.put("name", js.get(keyName));
                doc.put("code", keyName);
                System.out.println(keyName + ":" + js.get(keyName));
                updateList.add(doc);
            }
            _mss.write("stock_code", updateList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readRawData() throws UnsupportedEncodingException {

        try {
            byte[] fileBytes = Util.readFileContent2ByteArray(new File("/opt/hadoop/stock/raw/600503/600503_2015-01-06.json"));
            String jsonText = new String(fileBytes, "UTF-8");
            JSONArray js = new JSONArray(jsonText);
            for (int i = 0; i < js.length(); i++) {

                System.out.println(js.get(i).toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
