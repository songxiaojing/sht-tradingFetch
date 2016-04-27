package com.byw.stock.house.track.trading.fetch.client;

import com.byw.stock.house.track.trading.fetch.api.HttpClientReferent;
import com.byw.stock.house.track.trading.fetch.api.common.HttpClient;
import com.byw.stock.house.track.trading.fetch.client.listener.StockRawDataResponseListener;
import com.byw.stock.house.track.trading.fetch.services.MongoDBStorageService;
import com.byw.stock.house.platform.log.PlatformLogger;
import io.netty.handler.codec.http.HttpHeaders;
import org.bson.Document;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * 
 * 取得一支股票的每笔交易明细.
 * 
 * 指定时间范围，股票代码.
 * 
 * @title C2_StockRawDataFetchClient
 * @package com.byw.stock.house.track.client
 * @author baiyanwei
 * @version
 * @date Feb 16, 2016
 * 
 */
public class C2_StockDailyRawDataFetchClient extends AppClient {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(C2_StockDailyRawDataFetchClient.class);
    //
    final public static SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    final public static long DAY_INTERVAL = 1000 * 60 * 60 * 24;
    //
    final private MongoDBStorageService _mssRaw;
    final private MongoDBStorageService _mssCode;
    final private MongoDBStorageService _mssFetchRecord;

    /**
     * 股票数据拉取开始时间
     */
    public final Date _startDay;
    /**
     * 拉取天数长度.
     */
    public final int _days = 1;

    public C2_StockDailyRawDataFetchClient(MongoDBStorageService mssRaw, MongoDBStorageService mssCode, MongoDBStorageService mssFetchRecord, Date startDay) {

        this._mssRaw = mssRaw;
        this._mssCode = mssCode;
        this._startDay = startDay;
        this._mssFetchRecord = mssFetchRecord;
        _logger.info("create stock raw module fetch ,time range:" + startDay.toString() + " with " + _days + " day");

    }

    /**
     * 判断指定日期的股票数据是否已经拉取过.
     * 
     * @param stockCode
     * @param date
     * @return
     */
    private boolean isFetched(String stockCode, String date) {

        Document resultDoc = new Document();
        resultDoc.put("d", date);
        resultDoc.put("r", "record");
        List<Document> resultList = _mssFetchRecord.read(stockCode + "_fetch_record", resultDoc, new Document(), 0, 10);
        if (resultList != null && resultList.isEmpty() == false) {
            return true;
        }
        return false;
    }

    /**
     * 取得股票每笔交易明细.
     * 
     */
    @Override
    public void doClient() throws Exception {

        _logger.info("start fetch raw module for ,time range:" + _startDay.toString() + " with " + _days + " day");
        //

        List<Document> stockList = _mssCode.read("stock_code", new Document(), new Document(), 0, 10000);

        if (stockList == null) {
            return;
        }
        //
        long startPoint = _startDay.getTime();

        for (int s = 0; s < stockList.size(); s++) {
            try {
                Document doc = stockList.get(s);
                //{ "_id" : ObjectId("56ab04b6c36f4f139cdae1b5"), "name" : "粤水电", "code" : "sz002060" }
                String stockCode = doc.getString("code");
                if (stockCode == null || stockCode.length() == 0) {
                    continue;
                }
                for (int i = 0; i < _days; i++) {
                    //计算拉取的时间，以天为单位从后向前
                    Date requestDate = new Date(startPoint - i * DAY_INTERVAL);
                    String requestDateText = DEFAULT_DATE_FORMATTER.format(requestDate);
                    //判断当天的数据是否拉取过
                    if (isFetched(stockCode, requestDateText) == true) {
                        _logger.info("Skip fetching " + stockCode + "/" + requestDateText + ",it is exist.");
                        continue;
                    }
                    //启动HTTP客户端，进行拉取数据
                    _logger.info("Start to fetch " + stockCode + "/" + requestDateText);
                    //
                    HttpClientReferent hcr = HttpClientReferent.buildHttpClientReferent("market.finance.sina.com.cn", "/downxls.php?date=" + requestDateText + "&symbol=" + stockCode, new StockRawDataResponseListener(_mssRaw, _mssFetchRecord, stockCode, requestDateText));
                    hcr._responseBodyCharset = Charset.forName("GBK");
                    //hcr.addRequestHeader(HttpHeaders.Names.USER_AGENT, "BYW");
                    hcr.addRequestHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-US,en;q=0.8");
                    hcr.addRequestHeader(HttpHeaders.Names.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    HttpClient hc = new HttpClient(hcr);
                    //
                    try {
                        hc.start();
                        _logger.info("Fetched and updated " + stockCode + "/" + requestDateText);
                    } catch (Exception e) {
                        _logger.exception(e);
                    }
                }
            } catch (Exception e) {
                _logger.exception(e);
            }
        }
    }

    public static void main(String[] args) {

        try {
            MongoDBStorageService mssRaw = new MongoDBStorageService("192.168.72.36:27017", "STOCK_RAW");
            mssRaw.start();

            MongoDBStorageService mssCode = new MongoDBStorageService("192.168.72.36:27017", "STOCK");
            mssCode.start();

            MongoDBStorageService mssFetchRecord = new MongoDBStorageService("192.168.72.36:27017", "STOCK_RAW_FETCH_RECORD");
            mssFetchRecord.start();
            //
            Date startDay = DEFAULT_DATE_FORMATTER.parse("2016-02-15");
            //
            C2_StockDailyRawDataFetchClient srdf = new C2_StockDailyRawDataFetchClient(mssRaw, mssCode, mssFetchRecord, startDay);
            srdf.doClient();
            //
        } catch (Exception e) {
            _logger.exception(e);
        }
    }
}
