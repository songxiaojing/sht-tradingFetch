package com.byw.stock.house.track.client;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;

import com.byw.stock.house.track.api.HttpClientReferent;
import com.byw.stock.house.track.api.common.HttpClient;
import com.byw.stock.house.track.client.listener.StockCodeResponseListener;
import com.byw.stock.house.track.services.MongoDBStorageService;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;


/**
 * 
 * 取得所有股票代码与市值信息.
 * 
 * 代码，名称，盘大小，流通盘大小.
 * 
 * @title FetchAllStockCode
 * @package com.byw.stock.house.track.client
 * @author baiyanwei
 * @version
 * @date Feb 16, 2016
 * 
 */
public class C0_FetchAllStockCode extends AppClient {

    final public static SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    final private static PlatformLogger _logger = PlatformLogger.getLogger(C0_FetchAllStockCode.class);
    final private MongoDBStorageService _mss;

    public C0_FetchAllStockCode(MongoDBStorageService mss) {

        this._mss = mss;
    }

    @Override
    public void doClient() throws Exception {

        backupAndClearStockCode();

        //启动HTTP客户端
        HttpClientReferent hcr = HttpClientReferent.buildHttpClientReferent("ctxalgo.com", "/api/stocks", new StockCodeResponseListener(_mss));
        hcr._responseBodyCharset = Charset.forName("UTF-8");
        HttpClient hc = new HttpClient(hcr);
        //
        hc.start();
        _logger.info("Fetch and update STOCK_CODE is Done.");

    }

    /**
     * 备份与清理已经存在的股票代码数据，先备份现在股票代码信息，已经存在备份的直接删除原数据.
     * 
     */
    private void backupAndClearStockCode() {

        MongoCollection<Document> stock_code = _mss.getStockCollection("stock_code");
        if (stock_code == null || stock_code.count() == 0) {
            return;
        }
        try {
            //备份数据
            stock_code.renameCollection(new MongoNamespace("STOCK_HISTORY", "stock_code_" + DEFAULT_DATE_FORMATTER.format(new Date())));
        } catch (MongoCommandException mce) {
            _logger.exception(mce);
            //if target exists,drop it.
            stock_code.drop();
            //stock_code.dropCollection();
        }
    }

    public static void main(String[] args) {

        try {
            //指定存储数据库
            MongoDBStorageService mss = new MongoDBStorageService("192.168.72.36:27017", "STOCK");
            mss.start();
            //
            new C0_FetchAllStockCode(mss).doClient();
            //
        } catch (Exception e) {
            _logger.exception(e);
        }
    }
}
