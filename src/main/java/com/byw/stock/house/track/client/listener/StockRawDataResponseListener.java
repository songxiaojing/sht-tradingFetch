package com.byw.stock.house.track.client.listener;

import com.byw.stock.house.track.api.HttpClientReferent;
import com.byw.stock.house.track.api.IResponseListener;
import com.byw.stock.house.track.services.MongoDBStorageService;
import com.byw.stock.house.platform.log.PlatformLogger;
import org.bson.Document;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 分析股票代码与名称，并启动HTTP客户端取得盘面信息.
 * 
 * 总盘面，流通盘面.
 * 
 * @title StockCodeResponseListener
 * @package com.byw.stock.house.track.client.listener
 * @author baiyanwei
 * @version
 * @date Feb 16, 2016
 * 
 */
public class StockRawDataResponseListener implements IResponseListener {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(StockRawDataResponseListener.class);

    final public static MathContext BIGDECIMAL_POINT_4 = new MathContext(4);
    final private MongoDBStorageService _mssRaw;
    final private MongoDBStorageService _mssFetchRecord;
    //
    public SimpleDateFormat dateFormattor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String _stockCode = null;
    private String _date = null;

    public StockRawDataResponseListener(MongoDBStorageService mssRaw, MongoDBStorageService mssFetchRecord, String stockCode, String date) {

        this._stockCode = stockCode;
        this._date = date;
        this._mssRaw = mssRaw;
        this._mssFetchRecord = mssFetchRecord;
    }

    /**
     * 构建响应数据存储到数据库中.
     * 
     * @param stockCode
     * @param date
     * @param content
     * @return
     */
    public void buildResponseToMongoDB(String stockCode, String date, String content) {

        _logger.info("build response file for stockCode:" + stockCode + ",date:" + date + " content size:" + content.length());
        //
        String[] transactionRawData = content.split("\n");
        List<Document> saveList = new ArrayList<Document>();

        //
        for (int i = 1; i < transactionRawData.length; i++) {
            if (transactionRawData[i] == null || transactionRawData[i].trim().length() == 0) {
                continue;
            }
            Document transaction = transformEachTransaction(stockCode, date, transactionRawData[i].trim().split("\t"));
            if (transaction == null) {
                continue;
            }
            saveList.add(transaction);
        }
        try {
            this._mssRaw.write(stockCode, saveList);
            _logger.info("write record in MongoDB:" + stockCode + "," + date + ",size:" + saveList.size());
            updateFetchResult("record", "Transaction size:" + saveList.size());
        } catch (Exception e) {
            _logger.exception(e);
        }

    }

    /**
     * 更新拉取结果.
     * 
     * @param result
     * @param message
     */
    private void updateFetchResult(String result, String message) {

        try {
            Document resultDoc = new Document();
            resultDoc.put("d", this._date);
            resultDoc.put("r", result);
            resultDoc.put("m", message);
            this._mssFetchRecord.write(_stockCode + "_fetch_record", resultDoc);
        } catch (Exception e) {
            _logger.exception(e);
        }
    }

    /**
     * 构建每笔交易数据.
     * 
     * @param stockCode
     * @param date
     * @param rawData
     * @return
     */
    private Document transformEachTransaction(String stockCode, String date, String[] rawData) {

        if (rawData == null || rawData.length != 6) {
            return null;
        }
        //check
        for (String checkData : rawData) {
            if (checkData == null || checkData.trim().length() == 0) {
                return null;
            }
        }
        try {
            Document record = new Document();
            //成交时间  成交价 价格变动    成交量(手)  成交额(元)  性质
            //15:00:05    7.24    -0.01   0   0   买盘/卖盘/中性盘
            //14:59:45    7.25    --  244 176900  买盘
            //14:59:40    7.25    0.01    84  60900   买盘
            //14:13:44    8.42    -0.01   256 215552  卖盘
            //14:13:37    8.43    --  14  11802   买盘
            //14:13:35    8.43    --  1   843 买盘
            //14:13:28    8.43    --  44  37092   中性盘
            //14:13:24    8.43    --  178 150054  卖盘
            //14:13:18    8.43    -0.01   4   3372    卖盘
            String time = date + " " + rawData[0].trim();
            long timePoint = dateFormattor.parse(time).getTime();

            float price = Float.parseFloat(rawData[1].trim());
            float priceFloat = rawData[2].trim().equals("--") == true ? 0.0f : Float.parseFloat(rawData[2].trim());
            long hands = Long.parseLong(rawData[3].trim());
            long amount = Long.parseLong(rawData[4].trim());
            String action = "N";
            //
            if ("买盘".equals(rawData[5].trim()) == true) {
                action = "B";
            } else if ("卖盘".equals(rawData[5].trim()) == true) {
                action = "S";
            } else if ("中性盘".equals(rawData[5].trim()) == true) {
                if (priceFloat > 0.0f) {
                    action = "B";
                } else if (priceFloat < 0.0f) {
                    action = "S";
                }
            }
            //{ "_id" : ObjectId("56a595f0c36f4f114da21bdf"), "c" : "sh601890", "tp" : NumberLong("1453445914000"), "t" : "2016-01-22 14:58:34", "p" : 10.11, "f" : 0, "h" : NumberLong(124), "a" : NumberLong(125364), "an" : "B" }
            //timepoint 成交时间  成交价 价格变动    成交量(手)  成交额(元)  性质
            record.put("c", stockCode);
            record.put("tp", timePoint);
            record.put("t", time);
            record.put("p", new BigDecimal(price, BIGDECIMAL_POINT_4).doubleValue());
            record.put("f", new BigDecimal(priceFloat, BIGDECIMAL_POINT_4).doubleValue());
            record.put("h", hands);
            record.put("a", amount);
            record.put("an", action);
            return record;
        } catch (Exception e) {
            _logger.exception(e);
        }
        return null;
    }

    @Override
    public void fireDone(HttpClientReferent hcr, Object content) {

        if (content == null || hcr == null) {
            return;
        }
        if (_stockCode == null || _stockCode.length() == 0) {
            updateFetchResult("error", "_stockCode is empty.");
            return;
        }
        if (_date == null || _date.length() == 0) {
            updateFetchResult("error", "_date is empty.");
            return;
        }
        String text = (String) content;
        if (text.length() == 0) {
            updateFetchResult("error", "Response context is empty.");
            return;
        }
        if (hcr._responseStatus == null) {
            updateFetchResult("error", "HTTP Response is bad.");
            return;
        }
        if (hcr._responseStatus.code() < 200 || hcr._responseStatus.code() >= 300) {
            updateFetchResult("error", "HTTP Response code is " + hcr._responseStatus.code() + " .");
            return;
        }
        String contentDisposition = hcr._responseHeaderMap.get("Content-Disposition");
        if (contentDisposition == null || contentDisposition.length() == 0) {
            //
            updateFetchResult("error", "Can not find contentDisposition in response. context:" + text);
            return;
        }
        /*
        Date:Wed, 13 Jan 2016 03:30:31 GMT
        Server:Apache
        Set-Cookie:U_TRS1=0000001d.68d0170.5695c4d7.ebe39778; path=/; expires=Sat, 10-Jan-26 03:30:31 GMT; domain=.sina.com.cn
        Content-Disposition:attachment; filename="sh600900_ﾳ￉ﾽﾻￃ￷ￏﾸ_2011-07-08.xls"
        Cache-Control:max-age=120
        Expires:Wed, 13 Jan 2016 03:32:31 GMT
        DPOOL_HEADER:10.73.19.85
        Connection:close
        Transfer-Encoding:chunked
        Content-Type:application/vnd.ms-excel
        SINA-LB:aGEuNzIuZzEudGMubGIuc2luYW5vZGUuY29t
        SINA-TS:YTRmMTlhY2UgMCAwIDAgOCAxNTMK
        */
        String responseStockName = null;
        String responseStockDate = null;
        String[] fileNames = contentDisposition.split(";");
        for (int i = 0; i < fileNames.length; i++) {
            String data = fileNames[i].trim();
            if (data.startsWith("filename=") == true) {
                responseStockName = data.substring(data.indexOf("\"") + 1, data.indexOf("_"));
                responseStockDate = data.substring(data.lastIndexOf("_") + 1, data.indexOf("."));
                break;
            }
        }
        //if have no data ,return;
        if (responseStockName == null || responseStockDate == null || responseStockName.length() == 0 || responseStockDate.length() == 0) {
            updateFetchResult("error", "Can not find data time or stock code from HTTP header,response code:" + hcr._responseStatus.code() + ", for " + this._stockCode + "/" + this._date);
            return;
        }
        _logger.info("Fetch data and size is " + text.length() + ", for " + responseStockDate);
        if (responseStockName.equalsIgnoreCase(this._stockCode) == false) {
            updateFetchResult("error", "Can not match stock code: response/" + responseStockName + ",target/" + _stockCode);
            return;
        }
        if (responseStockDate.equalsIgnoreCase(this._date) == false) {
            updateFetchResult("error", "Can not match stock date: response/" + responseStockDate + ",target/" + _date);
            return;
        }
        //
        this.buildResponseToMongoDB(_stockCode, _date, text);
    }

    @Override
    public void fireException(HttpClientReferent hcr, Throwable cause) {

        updateFetchResult("error", cause.toString());
        _logger.exception(cause);

    }

}
