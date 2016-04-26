package com.byw.stock.house.track.trading.fetch.client.listener;

import com.byw.stock.house.track.trading.fetch.api.HttpClientReferent;
import com.byw.stock.house.track.trading.fetch.api.IResponseListener;
import com.byw.stock.house.track.trading.fetch.api.common.HttpClient;
import com.byw.stock.house.track.trading.fetch.services.MongoDBStorageService;
import com.byw.stock.house.platform.log.PlatformLogger;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
public class StockCodeResponseListener implements IResponseListener {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(StockCodeResponseListener.class);

    final private MongoDBStorageService _mss;

    public StockCodeResponseListener(MongoDBStorageService mss) {

        this._mss = mss;
    }

    @Override
    public void fireDone(HttpClientReferent hcr, Object content) {

        if (content == null) {
            return;
        }
        String text = (String) content;
        if (text.length() == 0) {
            return;
        }
        try {
            // sz300487:N\u84dd\u6653
            JSONObject js = new JSONObject(text);
            Long timePoint = System.currentTimeMillis();
            List<Document> updateList = new ArrayList<Document>();
            for (Iterator<?> keyIter = js.keys(); keyIter.hasNext();) {
                String keyName = (String) keyIter.next();
                Document doc = new Document();
                doc.put("name", js.get(keyName));
                doc.put("code", keyName);
                doc.put("fetchTime", timePoint);
                _logger.info(doc.toString());
                updateList.add(doc);
            }
            _mss.write("stock_code", updateList);
            //
            fetchEachStockInfo(updateList);
        } catch (Exception e) {
            _logger.exception(e);
        }
    }

    @Override
    public void fireException(HttpClientReferent hcr, Throwable cause) {

        _logger.exception(cause);

    }

    /**
     * 取得每个股票的市值与流通值.
     * 
     * @param stockList
     */
    private void fetchEachStockInfo(final List<Document> stockList) {

        new Thread() {

            public void run() {

                try {
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
                } catch (Exception e) {
                    _logger.exception(e);
                }
                //
            }
        }.start();
    }
}
