package com.byw.stock.house.track.client;

import java.util.List;

import org.bson.Document;

import com.byw.stock.house.track.api.HttpClientReferent;
import com.byw.stock.house.track.api.common.HttpClient;
import com.byw.stock.house.track.client.listener.StockTotalShareResponseListener;
import com.byw.stock.house.track.services.MongoDBStorageService;


/**
 * 
 * Your class summary,end with '.'.
 * 
 * Your class Detail description,end with '.'.
 * 
 * @title C2_StockInfoClient
 * @package com.byw.stock.house.track.client
 * @author baiyanwei
 * @version
 * @date Feb 16, 2016
 * 
 */
public class C1_StockTotalShareUpateClient extends AppClient {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(C1_StockTotalShareUpateClient.class);
    final private MongoDBStorageService _mss;

    public C1_StockTotalShareUpateClient(MongoDBStorageService mss) {

        this._mss = mss;
    }

    public static void main(String[] args) {

        try {
            MongoDBStorageService _mss = new MongoDBStorageService("192.168.72.36:27017", "STOCK");
            _mss.start();
            new C1_StockTotalShareUpateClient(_mss).doClient();
        } catch (Exception e) {
            _logger.exception(e);
        }

    }

    @Override
    public void doClient() throws Exception {

        List<Document> stockList = _mss.read("stock_code", new Document(), new Document(), 0, 10000);
        //
        for (int i = 0; i < stockList.size(); i++) {
            try {
                Document doc = stockList.get(i);
                //{ "_id" : ObjectId("56ab04b6c36f4f139cdae1b5"), "name" : "粤水电", "code" : "sz002060" }
                String stockCode = doc.getString("code");
                //http://qt.gtimg.cn/q=s_sh601718
                HttpClientReferent hcr = HttpClientReferent.buildHttpClientReferent("qt.gtimg.cn", "/q=" + stockCode, new StockTotalShareResponseListener(_mss, stockCode));
                //hcr._responseBodyCharset = Charset.forName("UTF-8");
                HttpClient hc = new HttpClient(hcr);
                //
                hc.start();
            } catch (Exception e) {
                _logger.exception(e);
            }
        }

    }
}
