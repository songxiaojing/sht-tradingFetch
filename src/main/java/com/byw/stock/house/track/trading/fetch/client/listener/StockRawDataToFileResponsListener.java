package com.byw.stock.house.track.trading.fetch.client.listener;

import com.byw.stock.house.track.trading.fetch.api.HttpClientReferent;
import com.byw.stock.house.track.trading.fetch.api.IResponseListener;
import com.byw.stock.house.track.trading.fetch.utils.Util;
import com.byw.stock.house.platform.log.PlatformLogger;
import io.netty.util.CharsetUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;


/**
 * 
 * 分析股票代码与名称，并启动HTTP客户端取得盘面信息.
 * 
 * 总盘面，流通盘面.
 * 
 * @title StockCodeResponseListener
 * @package com.byw.stock.house.track.client.listeners
 * @author baiyanwei
 * @version
 * @date Feb 16, 2016
 * 
 */
public class StockRawDataToFileResponsListener implements IResponseListener {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(StockRawDataToFileResponsListener.class);

    final public static MathContext BIGDECIMAL_POINT_4 = new MathContext(4);
    final private String _storeFilePath;
    //
    public SimpleDateFormat dateFormattor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String _stockCode = null;
    private String _date = null;

    public StockRawDataToFileResponsListener(String storeFilePath, String stockCode, String date) {

        this._stockCode = stockCode;
        this._date = date;
        this._storeFilePath = storeFilePath;
    }

    /**
     * build the stock raw module with json.
     * 
     * @param stockCode
     * @param date
     * @param content
     * @return
     */
    public void buildResponseToFile(String stockCode, String date, String content) {

        _logger.info("build response file for stockCode:" + stockCode + ",date:" + date + " content size:" + content.length());
        JSONArray stockRawJson = new JSONArray();
        //
        String[] transactionRawData = content.split("\n");
        for (int i = 1; i < transactionRawData.length; i++) {
            if (transactionRawData[i] == null || transactionRawData[i].trim().length() == 0) {
                continue;
            }
            JSONArray transaction = transformEachTransaction(stockCode, date, transactionRawData[i].trim().split("\t"));
            if (transaction == null) {
                continue;
            }
            stockRawJson.put(transaction);
        }
        writeTextFile(stockCode, date, stockRawJson.toString());
    }

    /**
     * write text file.
     * 
     * @param stockCode
     * @param date
     * @param content
     */
    private void writeTextFile(String stockCode, String date, String content) {

        String fileName = stockCode + "_" + date + ".json";
        File wFile = new File(_storeFilePath + File.separator + stockCode + File.separator + fileName);

        FileOutputStream pw = null;
        try {
            if (wFile.exists() == false) {
                //check parent file ,if doesn't exists ,then create is .
                if (wFile.getParentFile().exists() == false && wFile.getParentFile().mkdirs() == true) {
                    _logger.info("create directory:" + wFile.getParentFile());
                }
                if (wFile.createNewFile() == true) {
                    _logger.info("create file:" + wFile);
                }
            }
            Util.writeByteContentToFile(content.getBytes(CharsetUtil.UTF_8), wFile);
            _logger.info("write file:" + wFile);
        } catch (Exception e) {
            _logger.exception(e);
        } finally {
            if (pw != null) {
                try {
                    pw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Your Methods description is in here.
     * 
     * @param stockCode
     * @param date
     * @param rawData
     * @return
     */
    private JSONArray transformEachTransaction(String stockCode, String date, String[] rawData) {

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
            JSONArray rawJSON = new JSONArray();
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
            //timepoint 成交时间  成交价 价格变动    成交量(手)  成交额(元)  性质
            rawJSON.put(stockCode);
            rawJSON.put(timePoint);
            rawJSON.put(time);
            rawJSON.put(new BigDecimal(price, BIGDECIMAL_POINT_4));
            rawJSON.put(new BigDecimal(priceFloat, BIGDECIMAL_POINT_4));
            rawJSON.put(hands);
            rawJSON.put(amount);
            rawJSON.put(action);
            return rawJSON;
        } catch (Exception e) {
            _logger.exception(e);
        }
        return null;
    }

    /**
     * 更新拉取结果.
     * 
     * @param result
     * @param message
     */
    private void updateFetchResult(String result, String message) {

        try {
            JSONObject resultDoc = new JSONObject();
            resultDoc.put("d", this._date);
            resultDoc.put("r", result);
            resultDoc.put("m", message);
            _logger.info(resultDoc.toString());
        } catch (Exception e) {
            _logger.exception(e);
        }
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
        //if have no module ,return;
        if (responseStockName == null || responseStockDate == null || responseStockName.length() == 0 || responseStockDate.length() == 0) {
            updateFetchResult("error", "Can not find module time or stock code from HTTP header,response code:" + hcr._responseStatus.code() + ", for " + this._stockCode + "/" + this._date);
            return;
        }
        _logger.info("Fetch module and size is " + text.length() + ", for " + responseStockDate);
        if (responseStockName.equalsIgnoreCase(this._stockCode) == false) {
            updateFetchResult("error", "Can not match stock code: response/" + responseStockName + ",target/" + _stockCode);
            return;
        }
        if (responseStockDate.equalsIgnoreCase(this._date) == false) {
            updateFetchResult("error", "Can not match stock date: response/" + responseStockDate + ",target/" + _date);
            return;
        }
        //
        this.buildResponseToFile(_stockCode, _date, text);
    }

    @Override
    public void fireException(HttpClientReferent hcr, Throwable cause) {

        updateFetchResult("error", cause.toString());
        _logger.exception(cause);

    }

}
